/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.*
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import javax.inject.Inject

/**
 * A [ViewModel] that handles all aspects of onboarding/loading/app configuration etc.
 */
class LoadingViewModel @Inject constructor(

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The loading repository.
     */
    private val appRepository: AppRepository

) : ViewModel(),
    OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * A backing [LiveData] holding the API version. Used to trigger other loading processes.
     */
    private val liveApiVersion = MutableLiveData<Int>().apply {
        value = BuildConfig.API_VERSION
    }

    /**
     * A [MediatorLiveData] holding the application [Api] information.
     */
    val liveApi: LiveData<DataUpdate<Api, Api>> =
        Transformations.switchMap(liveApiVersion) { apiVersion ->
            appRepository.getApi(apiVersion)
        }

    /**
     * A [LiveData] holding the selected book ID.
     */
    val bookIdData: MutableLiveData<Long> = MutableLiveData()

    // endregion Properties

    // region Constructors

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    // endregion Constructors

    // region Inherited methods

    /**
     * Unregisters as a shared preference change listener.
     */
    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Sets the new value in [bookIdData].
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            PREF_KEY_CURRENT_BOOK_ID -> {
                bookIdData.value = sharedPreferences.getLong(key, -1)
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Called when the user selects a book.
     */
    fun selectBook(bookId: Long) {
        sharedPreferences.edit()
            .putLong(PREF_KEY_CURRENT_BOOK_ID, bookId)
            .apply()
    }

    @Suppress("UNUSED")
    fun load() {
        // TODO This will be a massive and complicated method that will perform all loading
    }

    // endregion Methods

}
