/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.SharedPreferences
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.PREF_KEY_VERIFIED_BETA_KEY
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.dao.ContentDao
import io.igist.core.data.mapper.*
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.*
import io.igist.core.domain.model.BookMode.DEFAULT
import io.igist.core.domain.model.BookMode.REQUIRE_BETA_KEY
import io.igist.core.domain.model.FileCategory.CHAPTER_IMAGE
import io.igist.core.domain.model.FileCategory.SPUTNIK
import io.igist.core.domain.model.FileCategory.BADGE
import io.igist.core.domain.model.FileCategory.STOREFRONT
import retrofit2.Response
import java.util.concurrent.CancellationException

/**
 * A Retrofit-based implementation of [AppRepository].
 */
class AppRepositoryImpl(

    private val apiDao: ApiDao,

    private val contentDao: ContentDao,

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
        contentListNum: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<ContentList, ContentList>> {
        contentListTask?.cancel(true)
        return ContentListTask(
            contentDao,
            appWebservice,
            alwaysFetch
        ).apply {
            contentListTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookId,
            appVersion,
            contentListNum
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

    }

    /**
     * [DataTaskinator] that checks a beta key (entered or cached) against the server.
     */
    private class BetaKeyTask(

        private val appWebservice: AppWebservice,

        private val sharedPreferences: SharedPreferences,

        private val alwaysVerify: Boolean = true

    ) : DataTaskinator<Any?, String, String>() {

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

    }

    /**
     * [DataTaskinator] that retrieves book content metadata.
     */
    private class ContentListTask(

        private val contentDao: ContentDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Any, ContentList, ContentList>() {

        override fun doInBackground(vararg params: Any?): ResultUpdate<ContentList, ContentList> {
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0) as? Long?
                ?: throw IllegalArgumentException("No book ID passed to ContentListTask")
            val appVersion: Int = params.getOrNull(1) as? Int?
                ?: throw IllegalArgumentException("No app version passed to ContentListTask")
            val contentListNum: Int = params.getOrNull(2) as? Int?
                ?: throw IllegalArgumentException("No content list num passed to ContentListTask")

            // Retrieve any cached content
            var contentList: ContentList? = retrieveContentList(bookId, appVersion, contentListNum)

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
                remoteContentLists?.getOrNull(contentListNum - 1)?.let { remoteContentList ->
                    // Convert & insert remote content list into the local database
                    val localContentList =
                        remoteContentList.toLocalContentList(bookId, contentListNum)
                    val contentListId: Long = contentDao.insertContentList(localContentList)

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
                            contentDao.insertContentFiles(this)
                        } ?: run {
                            contentDao.removeContentFiles(contentListId, it.value)
                        }
                    }

                    // TODO Convert & insert store data

                    contentList = retrieveContentList(bookId, appVersion, contentListNum)
                }
            }

            // Check if cancelled
            if (isCancelled) return FailureUpdate(contentList, CancellationException(), data)

            //val localContentList = contentDao.retrieve(bookId, appVersion)
            //var contentList: ContentList? = localContentList.toContentListOrNull()

            return SuccessUpdate(contentList)
        }

        // region Methods

        private fun retrieveContentList(
            bookId: Long,
            appVersion: Int,
            contentListNum: Int
        ): ContentList? = contentDao.retrieveContentList(
            bookId,
            appVersion,
            contentListNum
        )?.let {
            // We have a (cached) LocalContentList, so let's retrieve the rest that we need
            // to build a ContentList
            val localChapterImageContentFiles =
                contentDao.retrieveContentFiles(it.id, CHAPTER_IMAGE.value)
            val localSputnikContentFiles =
                contentDao.retrieveContentFiles(it.id, SPUTNIK.value)
            val localBadgeContentFiles =
                contentDao.retrieveContentFiles(it.id, BADGE.value)
            val localStorefrontContentFiles =
                contentDao.retrieveContentFiles(it.id, STOREFRONT.value)

            // TODO Store items

            it.toContentList(
                localChapterImageContentFiles,
                localSputnikContentFiles,
                localBadgeContentFiles,
                localStorefrontContentFiles
            )
        }

        // endregion Methods

    }

    // endregion Nested/inner classes

}
