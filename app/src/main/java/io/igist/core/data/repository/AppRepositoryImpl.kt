/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.SharedPreferences
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig
import io.igist.core.BuildConfig.PREF_KEY_VERIFIED_BETA_KEY
import io.igist.core.data.local.dao.*
import io.igist.core.data.local.entity.*
import io.igist.core.data.mapper.*
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteChapter
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.*
import io.igist.core.domain.model.BookMode.DEFAULT
import io.igist.core.domain.model.BookMode.REQUIRE_BETA_KEY
import io.igist.core.domain.model.FileCategory.*
import retrofit2.Response
import xmlwise.Plist
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.URL
import java.util.*
import java.util.concurrent.CancellationException

/**
 * A Retrofit-based implementation of [AppRepository].
 */
class AppRepositoryImpl(

    private val apiDao: ApiDao,

    private val cardDao: CardDao,

    private val cardImageDao: CardImageDao,

    private val contentFileDao: ContentFileDao,

    private val contentListDao: ContentListDao,

    private val storeCollectionDao: StoreCollectionDao,

    private val storeDepartmentDao: StoreDepartmentDao,

    private val storeItemDao: StoreItemDao,

    private val appWebservice: AppWebservice,

    private val sharedPreferences: SharedPreferences

) : AppRepository {

    // region Properties

    private var apiTask: ApiTask? = null

    private var betaKeyTask: BetaKeyTask? = null

    private var contentListTask: ContentListTask? = null

    // endregion Properties

    // region Implemented methods

    override fun getApi(
        bookId: Long,
        apiVersion: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Api, Api>> {
        apiTask?.cancel(true)
        betaKeyTask?.cancel(true)
        return ApiTask(
            apiDao,
            appWebservice,
            alwaysFetch
        ).apply {
            apiTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookId,
            apiVersion
        )
    }

    override fun checkBetaKey(
        bookMode: BookMode,
        betaKey: String?,
        alwaysVerify: Boolean
    ): LiveData<DataUpdate<String, String>> {
        betaKeyTask?.cancel(true)
        return BetaKeyTask(
            appWebservice,
            sharedPreferences,
            alwaysVerify
        ).apply {
            betaKeyTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookMode,
            betaKey
        )
    }

    override fun getContentList(
        bookId: Long,
        appVersion: Int,
        index: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<ContentList, ContentList>> {
        contentListTask?.cancel(true)
        return ContentListTask(
            cardDao,
            cardImageDao,
            contentFileDao,
            contentListDao,
            storeDepartmentDao,
            storeCollectionDao,
            storeItemDao,
            appWebservice,
            alwaysFetch
        ).apply {
            contentListTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookId,
            appVersion,
            index
        )
    }

    // endregion Implemented methods

    // region Nested/inner classes

    /**
     * [DataTaskinator] that retrieves API information.
     */
    private class ApiTask(

        private val apiDao: ApiDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Any, Api, Api>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Any?): ResultUpdate<Api, Api> {
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0) as? Long?
                ?: throw IllegalArgumentException("No book ID passed to ApiTask")
            val apiVersion: Int = params.getOrNull(1) as? Int?
                ?: throw IllegalArgumentException("No API version passed to ApiTask")

            // Retrieve any cached api
            val localApi = apiDao.retrieve(bookId, apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(api, CancellationException(), data)

            // Fetch the latest API info
            if (api == null || alwaysFetch) {
                // If we have a cached Api, publish it
                if (api != null) publishProgress(api)

                val update: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(bookId, apiVersion).toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(api, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(api, update.e, data)
                }

                update.result?.body()?.run {
                    // Convert & insert remote Api into the local database
                    apiDao.insert(this.toLocalApi(bookId))

                    // Re-retrieve the newly-inserted Api from the local database
                    apiDao.retrieve(bookId, apiVersion)?.let {
                        api = it.toApi()
                    }
                }
            }

            return SuccessUpdate(api)
        }

        // endregion Inherited methods

    }

    /**
     * [DataTaskinator] that checks a beta key (entered or cached) against the server.
     */
    private class BetaKeyTask(

        private val appWebservice: AppWebservice,

        private val sharedPreferences: SharedPreferences,

        private val alwaysVerify: Boolean = true

    ) : DataTaskinator<Any?, String, String>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Any?): ResultUpdate<String, String> {
            // Extract arguments from params
            val bookMode: BookMode = params.getOrNull(0) as? BookMode?
                ?: throw IllegalArgumentException("No BookMode passed to BetaKeyTask")
            var betaKey: String? = params.getOrNull(1) as? String?

            // Check if cancelled
            if (isCancelled) return FailureUpdate(betaKey, CancellationException(), data)

            // Verify the beta key if the book mode requires it
            when (bookMode) {
                DEFAULT -> {
                    // No beta key is required. Remove any previously-verified beta key from
                    // shared preferences and return success with null beta key value
                    sharedPreferences.edit().remove(PREF_KEY_VERIFIED_BETA_KEY).apply()
                    betaKey = null
                }
                REQUIRE_BETA_KEY -> {
                    // If we have a beta key (whether user-entered or previously-validated),
                    // publish it
                    if (betaKey != null) publishProgress(betaKey)

                    when {
                        betaKey == null -> return FailureUpdate(
                            betaKey,
                            IgistException(ResultMessage.BETA_KEY_REQUIRED),
                            data
                        )
                        alwaysVerify -> {
                            val update: ResultUpdate<Void, Response<RemoteMessage>> =
                                appWebservice.betaKey(
                                    if (betaKey.isEmpty()) " " else betaKey // Avoid empty string
                                ).toResultUpdate()

                            // Check if cancelled or failure
                            when {
                                isCancelled -> return FailureUpdate(
                                    betaKey,
                                    CancellationException(),
                                    data
                                )
                                update is FailureUpdate ->
                                    return FailureUpdate(betaKey, update.e, data)
                            }

                            // Check the result message
                            val message = update.result?.body()?.resultMessage
                                ?: ResultMessage.UnknownResultMessage()
                            when (message) {
                                ResultMessage.SUCCESS -> sharedPreferences.edit()
                                    .putString(PREF_KEY_VERIFIED_BETA_KEY, betaKey)
                                    .apply()
                                else -> return FailureUpdate(
                                    null,
                                    IgistException(message),
                                    data
                                )
                            }
                        }
                    }
                }
            }

            return SuccessUpdate(betaKey)
        }

        // endregion Inherited methods

    }

    /**
     * [DataTaskinator] that retrieves book content metadata.
     */
    private class ContentListTask(

        private val cardDao: CardDao,

        private val cardImageDao: CardImageDao,

        private val contentFileDao: ContentFileDao,

        private val contentListDao: ContentListDao,

        private val storeDepartmentDao: StoreDepartmentDao,

        private val storeCollectionDao: StoreCollectionDao,

        private val storeItemDao: StoreItemDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Any, ContentList, ContentList>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Any?): ResultUpdate<ContentList, ContentList> {
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0) as? Long?
                ?: throw IllegalArgumentException("No book ID passed to ContentListTask")
            val appVersion: Int = params.getOrNull(1) as? Int?
                ?: throw IllegalArgumentException("No app version passed to ContentListTask")
            val index: Int = params.getOrNull(2) as? Int?
                ?: throw IllegalArgumentException("No content list index passed to ContentListTask")

            // Retrieve any cached content
            var contentList: ContentList? = retrieveContentList(bookId, appVersion, index)

            if (contentList == null || alwaysFetch) {
                // If we have a cached content list, publish it
                if (contentList != null) publishProgress(contentList)

                val update: ResultUpdate<Void, Response<List<RemoteContentList>>> =
                    appWebservice.content(bookId, appVersion).toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(contentList, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(contentList, update.e, data)
                }

                val remoteContentLists = update.result?.body()
                remoteContentLists?.getOrNull(index)?.let { remoteContentList ->
                    // Remove old content list
                    contentListDao.remove(bookId, index)

                    // Convert & insert remote content list into the local database
                    val localContentList =
                        remoteContentList.toLocalContentList(bookId, index)
                    val contentListId: Long = contentListDao.insert(localContentList)

                    // Convert & insert remote content files into the local database
                    FileCategory.values().forEach {
                        val localContentFiles = when (it) {
                            CHAPTER_IMAGE -> remoteContentList.chapterImages
                            SPUTNIK -> remoteContentList.sputniks
                            BADGE -> remoteContentList.badges
                            STOREFRONT -> remoteContentList.storefront
                            else -> null
                        }.toLocalContentFilesOrNull(contentListId, it)
                        localContentFiles?.run {
                            contentFileDao.insert(this)
                        } ?: run {
                            contentFileDao.removeAll(contentListId, it.value)
                        }
                    }

                    // Convert & insert store data into the local database
                    storeDepartmentDao.removeAll(contentListId)
                    remoteContentList.storeData?.entries?.forEachIndexed { departmentIndex, entry ->
                        // Store each key as a LocalStoreDepartment
                        val departmentId: Long = storeDepartmentDao.insert(
                            LocalStoreDepartment(contentListId, departmentIndex, entry.key)
                        )

                        // Each value is a list of (remote) store collections
                        entry.value.forEachIndexed { categoryIndex, map ->
                            map.entries.forEachIndexed { collectionIndex, entry ->
                                val collectionId = storeCollectionDao.insert(
                                    LocalStoreCollection(
                                        departmentId,
                                        categoryIndex,
                                        collectionIndex,
                                        entry.key
                                    )
                                )

                                val localStoreItems: List<LocalStoreItem> =
                                    entry.value.toLocalStoreItems(collectionId)
                                storeItemDao.insert(localStoreItems)
                            }
                        }
                    }

                    // Convert & insert card data into the local database
                    cardDao.removeAll(contentListId)
                    remoteContentList.cardData?.entries?.forEachIndexed { cardIndex, entry ->
                        // In our remote card structure, each card name is repeated as the
                        // attribute name itself and also as the "name" field of each card
                        val localCard = entry.value.toLocalCard(contentListId, cardIndex)
                        val cardId: Long = cardDao.insert(localCard)

                        entry.value.images.forEachIndexed { imageIndex, imageName ->
                            val localCardImage = LocalCardImage(cardId, imageIndex, imageName)
                            cardImageDao.insert(localCardImage)
                        }
                    }

                    contentList = retrieveContentList(bookId, appVersion, index)
                }
            }

            // Check if cancelled
            if (isCancelled) return FailureUpdate(contentList, CancellationException(), data)

            return SuccessUpdate(contentList)
        }

        // endregion Inherited methods

        // region Methods

        /**
         * Retrieves all the info from the local database necessary to construct a [ContentList].
         */
        private fun retrieveContentList(
            bookId: Long,
            appVersion: Int,
            index: Int
        ): ContentList? = contentListDao.retrieve(
            bookId,
            appVersion,
            index
        )?.let {
            // We have a (cached) LocalContentList, so let's retrieve the rest that we need
            // to build a ContentList
            val localChapterImageContentFiles =
                contentFileDao.retrieve(it.id, CHAPTER_IMAGE.value)
            val localSputnikContentFiles =
                contentFileDao.retrieve(it.id, SPUTNIK.value)
            val localBadgeContentFiles =
                contentFileDao.retrieve(it.id, BADGE.value)
            val localStorefrontContentFiles =
                contentFileDao.retrieve(it.id, STOREFRONT.value)
            val localStoreDepartments =
                storeDepartmentDao.retrieve(it.id)
            val localStoreCollections = storeCollectionDao.retrieve(
                localStoreDepartments.map { it.id }
            )
            val localStoreItems = storeItemDao.retrieve(
                localStoreCollections.map { it.id }
            )
            val localCards = cardDao.retrieve(it.id)
            val localCardImages = cardImageDao.retrieve(
                localCards.map { it.id }
            )
            it.toContentList(
                bookId,
                localChapterImageContentFiles,
                localSputnikContentFiles,
                localBadgeContentFiles,
                localStorefrontContentFiles,
                localStoreDepartments,
                localStoreCollections,
                localStoreItems,
                localCards,
                localCardImages
            )
        }

        // endregion Methods

    }

    /**
     * [DataTaskinator] that retrieves book chapter(s).
     */
    private class BookContentTask(

        private val bookDao: BookDao,

        private val chapterDao: ChapterDao,

        private val alwaysFetch: Boolean

    ) : DataTaskinator<Any?, List<Chapter>?, List<Chapter>?>() {

        override fun doInBackground(vararg params: Any?):
                ResultUpdate<List<Chapter>?, List<Chapter>?> {
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0) as? Long?
                ?: throw IllegalArgumentException("No book ID passed to ApiTask")
            val chapterNumber: Int = params.getOrElse(1) {
                0
            } as Int

            // Retrieve the book to get plist file
            val plistFile: String = bookDao.retrieve(bookId)?.plistFile ?: return FailureUpdate(
                null,
                IllegalStateException("No book with bookID $bookId exists in local database")
            )

            // Retrieve any cached chapter(s)
            val localChapters: List<LocalChapter>? = when (chapterNumber) {
                0 -> chapterDao.retrieveAll(bookId)
                else -> ArrayList<LocalChapter>().apply {
                    chapterDao.retrieve(bookId, chapterNumber)?.also { add(it) }
                }
            }
            var chapters: List<Chapter>? = localChapters.toChaptersOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(chapters, CancellationException(), data)

            // Fetch the latest chapters
            if (chapters.isNullOrEmpty() || alwaysFetch) {
                // If we have cached chapters, publish them
                if (chapters != null) publishProgress(chapters)

                //val update: ResultUpdate<Void, Response<RemoteApi>> =
                //    appWebservice.api(bookId, apiVersion).toResultUpdate()



            }

            return SuccessUpdate(chapters)
        }

        @Throws(IOException::class)
        private fun retrieveChapters(
            plistFile: String,
            chapterNumber: Int
        ): List<RemoteChapter> {
            val url =
                URL("${BuildConfig.BASE_URL}/${BuildConfig.APP_FILES_DIRECTORY}/$plistFile")
            var chapterInputStream: InputStream? = null
            try {
                chapterInputStream = url.openStream().apply {
                    val byteArray = BufferedInputStream(this).readBytes()
                    /*
                    val byteArray = BufferedInputStream(this).run {
                        readBytes().also {
                            close()
                        }
                    }
                    */

                    // Resulting object should be a single-item array, so we will strip off
                    // the first element in order to create bookJson below since that's all
                    // we are interested in.
                    val resultObject = Plist.objectFromXml(String(byteArray))
                    /*
                    val resultJson = gson.toJsonTree(resultObject).asJsonArray[0]
                    val bookJson = JsonObject().apply {
                        addProperty(ID, BuildConfig.DEFAULT_BOOK_ID)
                        addProperty(TITLE, BuildConfig.BOOK_TITLE)
                        add(CHAPTERS, resultJson)
                    }
                    */


                }
            } catch (e: IOException) {
                throw e
            } finally {
                try {
                    chapterInputStream?.close()
                } catch (e: IOException) {
                    // No op
                }
            }



            TODO("Not yet implemented")
        }

    }

    // endregion Nested/inner classes

}
