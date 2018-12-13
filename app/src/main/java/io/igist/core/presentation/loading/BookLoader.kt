/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.KEY_DESCRIPTION
import io.igist.core.R
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.Book
import io.igist.core.domain.session.AppSessionManager
import javax.inject.Inject

/**
 * A class that loads all data and resources associated with a book.
 */
class BookLoader @Inject constructor(

    /**
     * The application context.
     */
    @ApplicationContext
    private val context: Context,

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
     * A [LiveData] that tracks updates associated with a loading the metadata for a [Book].
     */
    private var liveBookUpdate: LiveData<DataUpdate<Book, Book>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { bookUpdate ->
                    when (bookUpdate) {
                        is FailureUpdate -> {
                            // If we got a locally-cached book, fail silently and continue.
                            // Otherwise publish a FailureUpdate
                            if (bookUpdate.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    bookUpdate.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> setDescription(R.string.loading_progress_book_pending)
                        is ProgressUpdate -> {
                            book = bookUpdate.progress.getOrNull(0)
                            setDescription(R.string.loading_progress_book_progress)
                        }
                        is SuccessUpdate -> {
                            book = bookUpdate.result
                            setDescription(R.string.loading_progress_book_success)
                            loadingUpdate.removeSource(this) // TODO Do I need to do this? I suppose it's cleaner
                        }
                    }
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        data
                    )
                }
            }
        }

    private var liveApiUpdate: LiveData<DataUpdate<Api, Api>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { apiUpdate ->
                    when (apiUpdate) {
                        is FailureUpdate -> {
                            // If we got a locally-cached api, fail silently and continue.
                            // Otherwise publish a FailureUpdate
                            if (apiUpdate.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    apiUpdate.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate -> setDescription(R.string.loading_progress_api_pending)
                        is ProgressUpdate -> {
                            api = apiUpdate.progress.getOrNull(0)
                            setDescription(R.string.loading_progress_api_progress)
                        }
                        is SuccessUpdate -> {
                            api = apiUpdate.result
                            setDescription(R.string.loading_progress_api_success)
                            loadingUpdate.removeSource(this) // TODO Do I need to do this? I suppose it's cleaner
                        }
                    }
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        data
                    )
                }
            }
        }

    /**
     * A [LiveData] containing information about the current loading progress.
     */
    private val loadingUpdate: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    private var book: Book? = null
        set(value) {
            if (field != value) {
                Log.d("BookLoader", "book.set: value=$value")
                field = value
                appSessionManager.book = field
                field?.run {
                    // Get api
                    Log.d("BookLoader", "*********** Load api ***********")
                    liveApiUpdate = appRepository.getApi(id, apiVersion)
                }
            }
        }

    private var api: Api? = null
        set(value) {
            if (field != value) {
                Log.d("BookLoader", "api.set: value=$value")
                field = value
                appSessionManager.api = field
                field?.run {
                    // Check beta key
                }
            }
        }

    // endregion Properties

    // region Methods

    /**
     * Cancels this book load.
     */
    fun cancel() {
        cancelled = true
        // TODO ???
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
     * Sets the [KEY_DESCRIPTION] string in the [data] bundle.
     */
    private fun setDescription(
        @StringRes resId: Int,
        vararg formatArgs: Any
    ): Bundle = data.apply {
        putString(KEY_DESCRIPTION, context.getString(resId, formatArgs))
    }

    // endregion Methods

}
