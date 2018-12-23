/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.*
import io.igist.core.R
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.*
import io.igist.core.domain.model.FileCategory.*
import io.igist.core.domain.session.AppSessionManager
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A class that loads all data and resources associated with a book.
 */
@Singleton
class BookLoader @Inject constructor(

    /**
     * The application context.
     */
    @ApplicationContext
    private val context: Context,

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The book repository.
     */
    private val bookRepository: BookRepository,

    /**
     * The app repository.
     */
    private val appRepository: AppRepository,

    /**
     * The application session manager.
     */
    private val appSessionManager: AppSessionManager

) {

    // region Properties

    /**
     * An integer representing the current loading progress.
     */
    private var progress: Int = 0

    /**
     * An integer representing the maximum loading progress. If [max] is 0, it means we don't
     * yet know the maximum value and any progress bar showing the current progress should
     * display as indeterminate.
     */
    private var max: Int = 0

    /**
     * A [Bundle] to be passed back to an observer via [loadingUpdate].
     */
    private var data: Bundle = Bundle()

    /**
     * Whether this loader is cancelled.
     */
    private var cancelled: Boolean = false

    /**
     * A [DataTaskinator] for downloading content files.
     */
    private var fileDownloadTask: FileDownloadTask? = null

    /**
     * A [LiveData] that tracks updates associated with a loading the metadata for a [Book].
     */
    private var liveBookUpdate: LiveData<DataUpdate<Book, Book>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    when (update) {
                        is FailureUpdate -> {
                            // If we got a locally-cached book, fail silently and continue.
                            // Otherwise publish a FailureUpdate
                            if (update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    update.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            setDescription(R.string.loading_progress_book_pending)
                        )
                        is ProgressUpdate -> book = update.progress.getOrNull(0)
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                setDescription(R.string.loading_progress_book_success)
                            )
                            book = update.result
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    /**
     * A [LiveData] that tracks updates associated with a loading [Api] metadata.
     */
    private var liveApiUpdate: LiveData<DataUpdate<Api, Api>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    when (update) {
                        is FailureUpdate -> {
                            // If we got a locally-cached api, fail silently and continue.
                            // Otherwise publish a FailureUpdate
                            if (update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    update.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            setDescription(R.string.loading_progress_api_pending)
                        )
                        is ProgressUpdate -> api = update.progress.getOrNull(0)
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                setDescription(R.string.loading_progress_api_success)
                            )
                            api = update.result
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    private var liveBetaKeyUpdate: LiveData<DataUpdate<String, String>>? = null
        set(value) {
            field?.run {
                loadingUpdate.removeSource(this)
                betaKeyUpdate.value = PendingUpdate()
                betaKeyUpdate.removeSource(this)
            }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    when (update) {
                        is FailureUpdate -> {
                            // If we got an IgistException, publish the failure and exit.
                            // Otherwise, only publish if we don't have a previously-saved
                            // beta key
                            val e: Exception? = update.e
                            when (e) {
                                is IgistException ->
                                    data.putParcelable(KEY_RESULT_MESSAGE, e.resultMessage)
                            }
                            if (e is IgistException || update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    update.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            setDescription(R.string.loading_progress_beta_key_pending)
                        )
                        is ProgressUpdate -> { // No op
                        }
                        is SuccessUpdate -> {
                            // Publish a loading update
                            data.putParcelable(KEY_RESULT_MESSAGE, ResultMessage.SUCCESS)
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                setDescription(R.string.loading_progress_beta_key_success)
                            )
                            validatedBetaKey = update.result
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
                betaKeyUpdate.addSource(this) { update ->
                    betaKeyUpdate.value = update
                }
            }
        }

    private var liveContentListUpdate: LiveData<DataUpdate<ContentList, ContentList>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    when (update) {
                        is FailureUpdate -> {
                            // If we got a locally-cached content list, fail silently and continue.
                            // Otherwise publish a Failure Update
                            if (update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    update.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            setDescription(R.string.loading_progress_content_pending)
                        )
                        is ProgressUpdate -> contentList = update.progress.getOrNull(0)
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                setDescription(R.string.loading_progress_content_success)
                            )
                            contentList = update.result
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    private var liveFileDownloadUpdate: LiveData<DataUpdate<Any, Int>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    Log.d("BookLoader", "liveFileDownloadUpdate.onUpdate: update=$update")
                    when (update) {
                        is ProgressUpdate -> {
                            progress = (update.progress.getOrNull(0) as Int?) ?: progress
                            max = (update.progress.getOrNull(1) as Int?) ?: max
                            val message = (update.progress.getOrNull(2) as String?) ?: ""
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(progress, max),
                                setDescription(message)
                            )
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    /**
     * A [LiveData] containing information about the current loading progress.
     */
    val loadingUpdate: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    /**
     * A [LiveData] containing updates relating to validating a beta key.
     */
    val betaKeyUpdate: MediatorLiveData<DataUpdate<String, String>> = MediatorLiveData()

    /**
     * A [LiveData] containing the current [Book].
     */
    private var book: Book? = null
        set(value) {
            if (field != value) {
                field = value
                appSessionManager.book = field
                field?.run {
                    // Get the API associated with the selected book
                    liveApiUpdate = appRepository.getApi(id, apiVersion)
                }
            }
        }

    /**
     * A [LiveData] containing the [Api] associated with the current [Book].
     */
    private var api: Api? = null
        set(value) {
            if (field != value) {
                field = value
                appSessionManager.api = field
                field?.run {
                    // Check any saved beta key
                    liveBetaKeyUpdate = appRepository.checkBetaKey(
                        this.bookMode,
                        sharedPreferences.getString(PREF_KEY_VERIFIED_BETA_KEY, null)
                    )
                }
            }
        }

    /**
     * A [LiveData] containing information related to a validated beta key if one exists.
     */
    private var validatedBetaKey: String? = null
        set(value) {
            if (field != value) {
                field = value
                appSessionManager.validatedBetaKey = field
                field?.run {
                    // Go ahead with content loading
                    liveContentListUpdate = appRepository.getContentList(
                        book?.id ?: 0L,
                        book?.appVersion ?: 0
                    )
                }
            }
        }

    /**
     * A [LiveData] containing information related to the content list for the current book.
     */
    private var contentList: ContentList? = null
        set(value) {
            // Too complex to test for equality?
            if (field != value) {

                fileDownloadTask?.cancel(true) // TODO Does existing get removed as source somehow? I think it does if it's FailureResult due to cancellation
                liveFileDownloadUpdate = FileDownloadTask(
                    context,
                    progress,
                    max
                ).apply {
                    fileDownloadTask = this
                }.executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, field, value)

                field = value
                // Add to a manager?
                field?.run {
                    // TODO Do I need to do anything here?
                }
            } else {
                Log.i("BookLoader", "contentList::set(): value==field")
            }
        }

    // endregion Properties

    // region Methods

    /**
     * Cancels this book load.
     */
    fun cancel() {
        cancelled = true
        liveBookUpdate = null
        liveApiUpdate = null
        liveBookUpdate = null
    }

    /**
     * Loads the book corresponding to the supplied [bookId].
     */
    fun load(bookId: Long): LiveData<DataUpdate<Int, Boolean>> {
        progress = 0
        max = 0
        cancelled = false
        liveBookUpdate = bookRepository.getBook(bookId)
        return loadingUpdate
    }

    /**
     * Submits the supplied [betaKey] (and continues with the load if successful).
     */
    fun submitBetaKey(betaKey: String?): LiveData<DataUpdate<String, String>> {
        progress = 0
        max = 0
        cancelled = false

        // Check any saved beta key
        liveBetaKeyUpdate = appRepository.checkBetaKey(
            api?.bookMode ?: BookMode.DEFAULT,
            betaKey
        )

        return betaKeyUpdate
    }

    /**
     * Sets the [KEY_DESCRIPTION] string in the [data] bundle.
     */
    private fun setDescription(
        @StringRes resId: Int,
        vararg formatArgs: Any
    ): Bundle = setDescription(context.getString(resId, formatArgs))

    /**
     * Sets the [KEY_DESCRIPTION] string in the [data] bundle.
     */
    private fun setDescription(desc: String): Bundle = data.apply {
        putString(KEY_DESCRIPTION, desc)
    }

    // endregion Methods

    // region Nested/inner classes

    private class FileDownloadTask(
        val context: Context,
        var progress: Int,
        var max: Int
    ) : DataTaskinator<ContentList?, Any, Int>() {

        var currentFile: Int = 0
        var totalFiles: Int = 0

        override fun doInBackground(vararg params: ContentList?): ResultUpdate<Any, Int> {
            // Extract arguments from params
            val oldContentList: ContentList? = params.getOrElse(0) {
                throw IllegalArgumentException("No old ContentList passed to FileDownloadTask")
            }
            val newContentList: ContentList? = params.getOrElse(1) {
                throw IllegalArgumentException("No new ContentList passed to FileDownloadTask")
            }

            totalFiles = (newContentList?.chapterImages?.size ?: 0) +
                    (newContentList?.badges?.size ?: 0) +
                    (newContentList?.sputniks?.size ?: 0) +
                    (newContentList?.storefront?.size ?: 0)

            max += progress + totalFiles

            // Send the first update with a real max value
            publishProgress(
                progress,
                max,
                context.getString(R.string.loading_progress_file_download_pending)
            )

            syncFiles(CHAPTER_IMAGE, oldContentList?.chapterImages, newContentList?.chapterImages)
            syncFiles(SPUTNIK, oldContentList?.sputniks, newContentList?.sputniks)
            syncFiles(BADGE, oldContentList?.badges, newContentList?.badges)
            syncFiles(STOREFRONT, oldContentList?.storefront, newContentList?.storefront)

            return SuccessUpdate(progress)
        }

        private fun syncFiles(
            fileCategory: FileCategory,
            oldContentFiles: List<ContentFile>?,
            newContentFiles: List<ContentFile>?) {

            newContentFiles?.forEach {
                publishProgress(
                    ++progress,
                    max,
                    context.getString(
                        R.string.loading_progress_file_download_progress,
                        ++currentFile,
                        totalFiles,
                        it.filename
                    )
                )

                // TODO NEXT
                // TODO Make sure to put them in a directory specific to the book ID
            }
        }

    }

    // endregion Nested/inner classes

}
