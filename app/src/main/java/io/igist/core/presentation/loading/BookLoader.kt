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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.*
import io.igist.core.R
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.contract.ChapterRepository
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.*
import io.igist.core.domain.session.AppSessionManager
import java.io.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.net.URL
import java.util.concurrent.CancellationException
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
     * The chapter repository.
     */
    private val chapterRepository: ChapterRepository,

    /**
     * The application session manager.
     */
    private val appSessionManager: AppSessionManager

) {

    // region Properties

    /**
     * The current book ID.
     */
    private var bookId: Long = -1L

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
                                    -1,
                                    update.e,
                                    data.apply {
                                        putString(
                                            KEY_DESCRIPTION,
                                            context.getString(R.string.loading_progress_error)
                                        )
                                    }
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            data.apply {
                                putString(
                                    KEY_DESCRIPTION,
                                    context.getString(R.string.loading_progress_book_pending)
                                )
                            }
                        )
                        is ProgressUpdate -> book = update.progress.getOrNull(0)
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                data.apply {
                                    putString(
                                        KEY_DESCRIPTION,
                                        context.getString(R.string.loading_progress_book_success)
                                    )
                                }
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
                                    -1,
                                    update.e,
                                    data.apply {
                                        putString(
                                            KEY_DESCRIPTION,
                                            context.getString(R.string.loading_progress_error)
                                        )
                                    }
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            data.apply {
                                putString(
                                    KEY_DESCRIPTION,
                                    context.getString(R.string.loading_progress_api_pending)
                                )
                            }
                        )
                        is ProgressUpdate -> api = update.progress.getOrNull(0)
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                data.apply {
                                    putString(
                                        KEY_DESCRIPTION,
                                        context.getString(R.string.loading_progress_api_success)
                                    )
                                }
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
                                    -1,
                                    update.e,
                                    data.apply {
                                        putString(
                                            KEY_DESCRIPTION,
                                            context.getString(R.string.loading_progress_error)
                                        )
                                    }
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            data.apply {
                                putString(
                                    KEY_DESCRIPTION,
                                    context.getString(R.string.loading_progress_beta_key_pending)
                                )
                            }
                        )
                        is ProgressUpdate -> { // No op
                        }
                        is SuccessUpdate -> {
                            // Publish a loading update
                            data.putParcelable(KEY_RESULT_MESSAGE, ResultMessage.SUCCESS)
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                data.apply {
                                    putString(
                                        KEY_DESCRIPTION,
                                        context.getString(
                                            R.string.loading_progress_beta_key_success
                                        )
                                    )
                                }
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
                                    -1,
                                    update.e,
                                    data.apply {
                                        putString(
                                            KEY_DESCRIPTION,
                                            context.getString(R.string.loading_progress_error)
                                        )
                                    }
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(++progress, max),
                            data.apply {
                                putString(
                                    KEY_DESCRIPTION,
                                    context.getString(R.string.loading_progress_content_pending)
                                )
                            }
                        )
                        is ProgressUpdate -> {
                            if (!update.progress.isEmpty()) {
                                contentList = update.progress.getOrNull(0)
                            }
                        }
                        is SuccessUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(++progress, max),
                                data.apply {
                                    putString(
                                        KEY_DESCRIPTION,
                                        context.getString(R.string.loading_progress_content_success)
                                    )
                                }
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

    private var liveFileDownloadUpdate: LiveData<DataUpdate<Int, Int>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    val currentProgress: Int
                    val currentMax: Int
                    when {
                        update is ProgressUpdate -> {
                            currentProgress = extractProgress(update)
                            currentMax = extractMax(update)
                        }
                        loadingUpdate.value != null && loadingUpdate.value is ProgressUpdate -> {
                            currentProgress =
                                    extractProgress((loadingUpdate.value as ProgressUpdate))
                            currentMax = extractMax(loadingUpdate.value as ProgressUpdate)
                        }
                        else -> {
                            currentProgress = 0
                            currentMax = 0
                        }
                    }

                    when (update) {
                        is FailureUpdate -> {
                            // If we got a locally-cached content list, fail silently and continue.
                            // Otherwise publish a Failure Update
                            if (update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    -1,
                                    update.e,
                                    data.apply {
                                        putString(
                                            KEY_DESCRIPTION,
                                            context.getString(R.string.loading_progress_error)
                                        )
                                    }
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> loadingUpdate.value = ProgressUpdate(
                            arrayOf(currentProgress, currentMax),
                            data.apply {
                                putString(
                                    KEY_DESCRIPTION,
                                    context.getString(R.string.loading_progress_file_download_pending)
                                )
                            }
                        )
                        is ProgressUpdate -> {
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(currentProgress, currentMax),
                                update.data
                            )
                        }
                        is SuccessUpdate -> {
                            progress = currentProgress
                            max = currentMax
                            loadingUpdate.value = ProgressUpdate(
                                arrayOf(progress, max),
                                data.apply {
                                    putString(
                                        KEY_DESCRIPTION,
                                        context.getString(
                                            R.string.loading_progress_file_download_success
                                        )
                                    )
                                }
                            )

                            liveChapterListUpdate = chapterRepository.getChapters(bookId)
                        }
                    }
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    private var liveChapterListUpdate: LiveData<DataUpdate<List<Chapter>, List<Chapter>>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    // TODO NEXT !!!
                    when (update) {
                        is FailureUpdate -> {

                        }
                        is PendingUpdate -> {

                        }
                        is ProgressUpdate -> {

                        }
                        is SuccessUpdate -> {

                        }
                    }
                }
            }
        }

    /**
     * A [LiveData] containing information about the current loading progress.
     */
    val loadingUpdate: MediatorLiveData<DataUpdate<Int, Int>> = MediatorLiveData()

    /**
     * A [LiveData] containing updates relating to validating a beta key.
     */
    val betaKeyUpdate: MediatorLiveData<DataUpdate<String, String>> = MediatorLiveData()

    /**
     * The current [Book].
     */
    private var book: Book? = null
        set(value) {
            if (field != value) {
                field = value
                bookId = value?.id ?: -1L
                appSessionManager.book = field
                field?.run {
                    // Get the API associated with the selected book
                    liveApiUpdate = appRepository.getApi(id, apiVersion)
                }
            }
        }

    /**
     * The [Api] associated with the current [Book].
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
     * Information related to a validated beta key if one exists.
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
     * Backing property for [contentList].
     */
    //private var _contentList: ContentList? = null

    /**
     * The [ContentList] for the current book.
     */
    private var contentList: ContentList? = null
        //        get() = _contentList
        set(value) {
/*
            if (field != value) {
*/
            fileDownloadTask?.cancel(true) // TODO Does existing get removed as source somehow? I think it does if it's FailureResult due to cancellation
            liveFileDownloadUpdate = FileDownloadTask(
                context,
                bookId,
                progress,
                max
            ).apply {
                fileDownloadTask = this
            }.executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, field, value)

            field = value
            // Add to a manager?
            field?.run {
                // TODO Do I need to do anything here?
                Log.d(
                    "BookLoader",
                    "We got a real content list and I think we're in the process of downloading"
                )
            }
/*
            } else {
                liveFileDownloadUpdate = MutableLiveData<DataUpdate<Any, Int>>().apply {
                    this.value = SuccessUpdate(progress)
                }
            }
*/
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
    fun load(bookId: Long): LiveData<DataUpdate<Int, Int>> {
        /*
        progress = 0
        max = 0
        */
        cancelled = false
        liveBookUpdate = bookRepository.getBook(bookId, false)
        return loadingUpdate
    }

    /**
     * Submits the supplied [betaKey] (and continues with the load if successful).
     */
    fun submitBetaKey(betaKey: String?): LiveData<DataUpdate<String, String>> {
        /*
        progress = 0
        max = 0
        */
        cancelled = false

        // Check any saved beta key
        liveBetaKeyUpdate = appRepository.checkBetaKey(
            api?.bookMode ?: BookMode.DEFAULT,
            betaKey
        )

        return betaKeyUpdate
    }

    // endregion Methods

    // region Nested/inner classes

    private class FileDownloadTask(
        context: Context,
        val bookId: Long,
        var progress: Int,
        var max: Int
    ) : DataTaskinator<ContentList?, Int, Int>() {

        val contextRef = WeakReference<Context>(context)
        /*
        var currentFile: Int = 0
        var totalFiles: Int = 0
        */

        override fun doInBackground(vararg params: ContentList?): ResultUpdate<Int, Int> {
            val context: Context = contextRef.get()
                ?: throw IllegalStateException("Context is not available in FileDownloadTask")

            // Extract arguments from params
            val oldContentList: ContentList? = params.getOrElse(0) {
                throw IllegalArgumentException("No old ContentList passed to FileDownloadTask")
            }
            val newContentList: ContentList? = params.getOrElse(1) {
                throw IllegalArgumentException("No new ContentList passed to FileDownloadTask")
            }

            val filesToDownload: ArrayList<ContentFile> = ArrayList()
            val filesToDelete: ArrayList<ContentFile> = ArrayList()
            FileCategory.values().forEach { fileCategory ->
                if (isCancelled) {
                    return FailureUpdate(progress, CancellationException(), data)
                }

                val oldFiles: ArrayList<ContentFile>? =
                    oldContentList?.getContentFiles(fileCategory)?.let { contentFiles ->
                        ArrayList(contentFiles)
                    }
                val newFiles: List<ContentFile>? = newContentList?.getContentFiles(fileCategory)
                max += newFiles?.size ?: 0
                newFiles?.forEach { newFile ->
                    if (isCancelled) {
                        return FailureUpdate(progress, CancellationException(), data)
                    }

                    val localDir = newFile.fileCategory.getLocalDir(context, bookId)
                    val localFile = File("$localDir/${newFile.filename}")
                    val oldFile: ContentFile? = oldFiles?.find {
                        it.filename == newFile.filename
                    }
                    val download: Boolean = when {
                        !localFile.exists() -> true
                        oldFile != null -> oldFile.date < newFile.date
                        else -> false
                    }
                    oldFile?.run {
                        oldFiles.remove(this)
                    }
                    if (download) {
                        filesToDownload.add(newFile)
                    } else {
                        progress++
                    }
                }
                oldFiles?.run {
                    max += this.size
                    filesToDelete.addAll(this)
                }
            }

            if (isCancelled) {
                return FailureUpdate(progress, CancellationException(), data)
            }

            val fileCount = filesToDownload.size + filesToDelete.size
//            max += progress + fileCount

            // Send the first update with a real max value
            data.putString(
                KEY_DESCRIPTION,
                context.getString(R.string.loading_progress_file_download_pending)
            )
            publishProgress(progress, max)

            var fileNum = 0
            for (file in filesToDownload) {
                fileNum++

                if (isCancelled) {
                    return FailureUpdate(progress, CancellationException(), data)
                }

                data.putString(
                    KEY_DESCRIPTION,
                    context.getString(
                        R.string.loading_progress_file_download_progress,
                        fileNum,
                        fileCount,
                        file.filename
                    )
                )
                publishProgress(++progress, max)

                val networkDir = file.fileCategory.getNetworkDir(bookId)
                val localDir: String = file.fileCategory.getLocalDir(context, bookId)

                // Download the file
                try {
                    downloadFile(file, networkDir, localDir)
                } catch (e: Exception) {
                    return FailureUpdate(
                        progress,
                        e,
                        data.apply {
                            putString(
                                KEY_DESCRIPTION,
                                context.getString(R.string.loading_progress_error)
                            )
                        }
                    )
                }
            }

            filesToDelete.forEach { file ->
                val localDir: String = file.fileCategory.getLocalDir(context, bookId)
                deleteFile(file, localDir)
            }

            return SuccessUpdate(progress)
        }


        @Throws(IOException::class, FileNotFoundException::class)
        private fun downloadFile(
            file: ContentFile,
            networkDir: String,
            localDir: String
        ) {
            val url = URL("$networkDir/${file.filename}")
            val byteArray: ByteArray = BufferedInputStream(url.openStream()).run {
                readBytes().also {
                    close()
                }
            }
            val dir: File = File(localDir).apply {
                mkdirs()
            }
            FileOutputStream(File(dir, file.filename), false).apply {
                write(byteArray)
                close()
            }
        }

        private fun deleteFile(
            file: ContentFile,
            localDir: String
        ) {
            val localFile = File("$localDir/${file.filename}")
            localFile.delete()
        }

    }

    // endregion Nested/inner classes

    // region Companion object

    companion object {

        // region Methods

        private fun extractProgress(update: DataUpdate<Int, Int>): Int = when (update) {
            is ProgressUpdate -> update.progress.getOrNull(0) ?: 0
            else -> 0
        }

        private fun extractMax(update: DataUpdate<Int, Int>): Int = when (update) {
            is ProgressUpdate -> update.progress.getOrNull(1) ?: 0
            else -> 0
        }

        // endregion Methods

    }

    // endregion Companion object

}
