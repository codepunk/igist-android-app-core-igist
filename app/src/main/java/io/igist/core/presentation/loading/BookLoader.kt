/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.Book
import io.igist.core.domain.model.BookMode
import io.igist.core.domain.model.ResultMessage
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
                        is SuccessUpdate -> book = update.result
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
                        is SuccessUpdate -> api = update.result
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
                // ??? loadingUpdate.value = PendingUpdate()
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
                            // beta key.
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
                        is SuccessUpdate -> validatedBetaKey = update.result
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

    /**
     * A [LiveData] containing information about the current loading progress.
     */
    val loadingUpdate: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    val betaKeyUpdate: MediatorLiveData<DataUpdate<String, String>> = MediatorLiveData()

    private var book: Book? = null
        set(value) {
            if (field != value) {
                field = value
                appSessionManager.book = field
                field?.run {
                    // Publish a loading update
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        setDescription(R.string.loading_progress_book_success)
                    )

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
                    // Publish a loading update
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        setDescription(R.string.loading_progress_api_success)
                    )

                    // Check any saved beta key
                    liveBetaKeyUpdate = appRepository.checkBetaKey(
                        this.bookMode,
                        sharedPreferences.getString(PREF_KEY_VERIFIED_BETA_KEY, null)
                    )
                }
            }
        }

    private var validatedBetaKey: String? = null
        set(value) {
            if (field != value) {
                field = value
                appSessionManager.validatedBetaKey = field
                field?.run {
                    // Publish a loading update
                    data.putParcelable(KEY_RESULT_MESSAGE, ResultMessage.SUCCESS)
                    loadingUpdate.value = ProgressUpdate(
                        arrayOf(++progress, max),
                        setDescription(R.string.loading_progress_beta_key_success)
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
    ): Bundle = data.apply {
        putString(KEY_DESCRIPTION, context.getString(resId, formatArgs))
    }

    // endregion Methods

}
