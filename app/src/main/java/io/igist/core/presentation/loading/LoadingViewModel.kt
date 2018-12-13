/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import javax.inject.Inject
import javax.inject.Provider

/**
 * A [ViewModel] that handles all aspects of onboarding/loading/app configuration etc.
 */
class LoadingViewModel @Inject constructor(

    private val sharedPreferences: SharedPreferences,

    private val bookLoaderProvider: Provider<BookLoader>

) :
    ViewModel(),
    OnSharedPreferenceChangeListener {

    // region Properties

    private var bookLoader: BookLoader? = null

    val liveProgress: MediatorLiveData<DataUpdate<Int, Boolean>> =
        MediatorLiveData()

    private var liveLoadingProgressSource: LiveData<DataUpdate<Int, Boolean>>? = null
        set(value) {
            field?.run { liveProgress.removeSource(this) }
            field = value
            field?.run {
                liveProgress.addSource(this) { loadingProgress ->
                    liveProgress.value = loadingProgress
                }
            }
        }

    // endregion Properties

    // region Constructors

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (sharedPreferences.contains(PREF_KEY_CURRENT_BOOK_ID)) {
            onSharedPreferenceChanged(sharedPreferences, PREF_KEY_CURRENT_BOOK_ID)
        }
    }

    // endregion Constructors

    // region Inherited methods

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // endregion Inherited methods

    // region Implemented methods

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            PREF_KEY_CURRENT_BOOK_ID -> {
                val bookId = sharedPreferences.getLong(key, 0L)
                if (bookId > 0) {
                    bookLoader?.cancel()
                    bookLoaderProvider.get().apply {
                        bookLoader = this
                        liveLoadingProgressSource = load(bookId)
                    }
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods


    // endregion Methods

}
