/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import io.igist.core.BuildConfig
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A manager that keeps track of all app-related session information, for example, the currently-
 * selected book.
 */
@Singleton
class AppSessionManager @Inject constructor(

    sharedPreferences: SharedPreferences,

    private val bookRepository: BookRepository,

    private val appRepository: AppRepository

) : OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * An observable [LiveData] with updates related to the currently-selected [Book].
     */
    @Suppress("WEAKER_ACCESS")
    var bookUpdateData: MediatorLiveData<DataUpdate<Book, Book>> = MediatorLiveData()

    /**
     * A [LiveData] that serves as a data source for [bookUpdateData].
     */
    private var bookUpdateDataSource: LiveData<DataUpdate<Book, Book>>? = null
        set(value) {
            field?.run { bookUpdateData.removeSource(this) }
            field = value
            field?.run {
                bookUpdateData.addSource(this) { update -> bookUpdateData.value = update }
            }
        }

    /**
     * An observable [LiveData] with updates related to the current [Api].
     */
    @Suppress("WEAKER_ACCESS")
    var apiUpdateData: MediatorLiveData<DataUpdate<Api, Api>> = MediatorLiveData()

    /**
     * A [LiveData] that serves as a data source for [apiUpdateData].
     */
    private var apiUpdateDataSource: LiveData<DataUpdate<Api, Api>>? = null
        set(value) {
            field?.run { apiUpdateData.removeSource(this) }
            field = value
            field?.run {
                apiUpdateData.addSource(this) { update -> apiUpdateData.value = update }
            }
        }

    /**
     * The currently-selected [Book].
     */
    var book: Book? = null
        set(value) {
            if (field != value) {
                Log.d("AppSessionManager", "book.set: value=$value")
                field = value
                field?.run {
                    // apiUpdateDataSource = appRepository.load()
                    apiUpdateDataSource = appRepository.getApi(id, apiVersion)
                }
            }
        }

    /**
     * The [Api] instance containing application defaults for the currently-selected [Book].
     */
    var api: Api? = null
        set(value) {
            if (field != value) {
                Log.d("AppSessionManager", "api.set: value=$value")
                field = value
            }
        }

    /**
     * A [AppSession] instance for storing the current book session.
     */
    @Suppress("UNUSED")
    var appSession: AppSession? = null
        private set

    // endregion Properties

    // region Constructors

    init {
        // Set up observers
        bookUpdateData.observeForever { update -> onBookDataUpdate(update) }
        apiUpdateData.observeForever { update -> onApiDataUpdate(update) }

        // Set up shared preference listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (sharedPreferences.contains(BuildConfig.PREF_KEY_CURRENT_BOOK_ID)) {
            onSharedPreferenceChanged(sharedPreferences, BuildConfig.PREF_KEY_CURRENT_BOOK_ID)
        }
    }

    // endregion Constructors

    // region Implemented methods

    /**
     * Listens for changes to the currently-selected book and updates this manager accordingly.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            BuildConfig.PREF_KEY_CURRENT_BOOK_ID -> {
                val bookId = sharedPreferences.getLong(key, 0L)
                if (bookId > 0) {
                    bookUpdateDataSource = bookRepository.getBook(bookId)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Called on a change to [Book] data updates.
     */
    private fun onBookDataUpdate(update: DataUpdate<Book, Book>) {
        book = when (update) {
            is ProgressUpdate -> update.progress.getOrNull(0)
            is SuccessUpdate -> update.result
            is FailureUpdate -> null // TODO
            else -> null
        }
    }

    /**
     * Called on a change to [Api] data updates.
     */
    private fun onApiDataUpdate(update: DataUpdate<Api, Api>) {
        api = when (update) {
            is ProgressUpdate -> update.progress.getOrNull(0)
            is SuccessUpdate -> update.result
            is FailureUpdate -> null // TODO
            else -> null
        }
    }

    // endregion Methods

}
