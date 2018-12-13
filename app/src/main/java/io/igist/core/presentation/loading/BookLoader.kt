/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.KEY_DESCRIPTION
import io.igist.core.BuildConfig.PREF_KEY_VERIFIED_BETA_KEY
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
                        is PendingUpdate -> setDescription(R.string.loading_progress_book_pending)
                        is ProgressUpdate -> {
                            book = update.progress.getOrNull(0)
                            setDescription(R.string.loading_progress_book_progress)
                        }
                        is SuccessUpdate -> {
                            book = update.result
                            setDescription(R.string.loading_progress_book_success)
                        }
                    }
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        data
                    )
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

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
                        is PendingUpdate -> setDescription(R.string.loading_progress_api_pending)
                        is ProgressUpdate -> {
                            api = update.progress.getOrNull(0)
                            setDescription(R.string.loading_progress_api_progress)
                        }
                        is SuccessUpdate -> {
                            api = update.result
                            setDescription(R.string.loading_progress_api_success)
                        }
                    }
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        data
                    )
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
                }
            }
        }

    private var liveBetaKeyUpdate: LiveData<DataUpdate<String, String>>? = null
        set(value) {
            field?.run { loadingUpdate.removeSource(this) }
            field = value
            field?.run {
                loadingUpdate.addSource(this) { update ->
                    when (update) {
                        is FailureUpdate -> {
                            // If we have a previously-validated beta key, fail silently and
                            // continue. Otherwise publish a FailureUpdate
                            if (update.result == null) {
                                loadingUpdate.value = FailureUpdate(
                                    false,
                                    update.e,
                                    setDescription(R.string.loading_progress_error)
                                )
                                return@addSource
                            }
                        }
                        is PendingUpdate ->
                            setDescription(R.string.loading_progress_beta_key_pending)
                        is ProgressUpdate ->
                            setDescription(R.string.loading_progress_beta_key_progress)
                        is SuccessUpdate -> {
                            setDescription(R.string.loading_progress_beta_key_success)
                            sharedPreferences.edit()
                                .putString(PREF_KEY_VERIFIED_BETA_KEY, update.result)
                                .apply()
                        }
                    }
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        data
                    )
                    if (update is ResultUpdate) {
                        loadingUpdate.removeSource(this)
                    }
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
                field = value
                appSessionManager.book = field
                field?.run {
                    // Get the API associated with the selected book
                    liveApiUpdate = appRepository.getApi(id, apiVersion)
                }
            }
        }

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
