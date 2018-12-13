/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.*
import io.igist.core.di.qualifier.ApplicationContext
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

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val bookRepository: BookRepository,

    private val appRepository: AppRepository

) : OnSharedPreferenceChangeListener {

    // TODO Maybe maybe maybe move a lot of this logic into a service?
    // - OR - Seriously: Maybe there's a way to put all of this in a single "load(bookId)" method.

    // region Properties

    private var loadingStep: Int = 0

    private var loadingStepTotal: Int = 0

    private var loadingBundle: Bundle = Bundle()

    /**
     * A [LiveData] containing information about the loading process.
     */
    var loadingData: MutableLiveData<DataUpdate<Int, Boolean>> =
        MutableLiveData<DataUpdate<Int, Boolean>>().apply {
            value = PendingUpdate()
        }

    /**
     * A [LiveData] containing updates related to the currently-selected [Book].
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
                bookUpdateData.addSource(this) { update ->
                    bookUpdateData.value = update
                }
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
                    apiUpdateDataSource = appRepository.getApi(id, apiVersion)
                }
            }
        }

    /**
     * A [LiveData] containing updates related to the current [Api].
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
                apiUpdateData.addSource(this) { update ->
                    apiUpdateData.value = update
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
                field?.run {
                    betaKeyUpdateDataSource = appRepository.checkBetaKey(
                        bookMode,
                        sharedPreferences.getString(PREF_KEY_VERIFIED_BETA_KEY, null)
                    )
                }
            }
        }

    /**
     * A [LiveData] containing updates related to validating the beta key.
     */
    @Suppress("WEAKER_ACCESS")
    var betaKeyUpdateData: MediatorLiveData<DataUpdate<String, String>> = MediatorLiveData()

    /**
     * A [LiveData] that serves as a data source for [betaKeyUpdateData].
     */
    private var betaKeyUpdateDataSource: LiveData<DataUpdate<String, String>>? = null
        set(value) {
            field?.run { betaKeyUpdateData.removeSource(this) }
            field = value
            field?.run {
                betaKeyUpdateData.addSource(this) { update ->
                    betaKeyUpdateData.value = update
                }
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
//        bookUpdateData.observeForever { update -> onBookDataUpdate(update) }
//        apiUpdateData.observeForever { update -> onApiDataUpdate(update) }

        // Set up shared preference listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (sharedPreferences.contains(PREF_KEY_CURRENT_BOOK_ID)) {
            onSharedPreferenceChanged(sharedPreferences, PREF_KEY_CURRENT_BOOK_ID)
        }
    }

    // endregion Constructors

    // region Implemented methods

    /**
     * Listens for changes to the currently-selected book and updates this manager accordingly.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            PREF_KEY_CURRENT_BOOK_ID -> {
                /*
                val bookId = sharedPreferences.getLong(key, 0L)
                if (bookId > 0) {
                    loadingStep = 0
                    loadingStepTotal = 0
                    //setDescription(context.getString(R.string.loading_book))
                    loadingData.value = ProgressUpdate(
                        arrayOf(loadingStep, loadingStepTotal),
                        loadingBundle
                    )
                    bookUpdateDataSource = bookRepository.getBook(bookId)
                }
                */
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Called on a change to [Book] data updates.
     */
    private fun onBookDataUpdate(update: DataUpdate<Book, Book>) {
        if (update is ResultUpdate) {
            if (update is FailureUpdate && update.result == null) {
                loadingData.value = FailureUpdate(false, update.e, update.data)
            } else {
                //setDescription(context.getString(R.string.loading_book_loaded))
                loadingData.value = ProgressUpdate(
                    arrayOf(++loadingStep, loadingStepTotal),
                    loadingBundle
                )
            }
        }

        book = when (update) {
            is ProgressUpdate -> update.progress.getOrNull(0)
            is SuccessUpdate -> update.result
            is FailureUpdate -> null
            else -> null
        }
    }

    /**
     * Called on a change to [Api] data updates.
     */
    private fun onApiDataUpdate(update: DataUpdate<Api, Api>) {
        when (update) {
            is PendingUpdate -> {}
            is ProgressUpdate -> api = update.progress.getOrNull(0)
            is SuccessUpdate -> api = update.result
            is FailureUpdate -> {}
            else -> {}
        }
    }

    private fun setDescription(description: String) {
        loadingBundle.putString(KEY_DESCRIPTION, description)
    }

    // endregion Methods

}
