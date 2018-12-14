/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import javax.inject.Inject

/**
 * A [ViewModel] that handles all aspects of onboarding/loading/app configuration etc.
 */
class LoadingViewModel @Inject constructor(

    private val sharedPreferences: SharedPreferences,

    private val bookLoader: BookLoader

) :
    ViewModel(),
    OnSharedPreferenceChangeListener {

    // region Properties

    val liveProgress: LiveData<DataUpdate<Int, Boolean>> = bookLoader.loadingUpdate

    val liveBetaKey: LiveData<DataUpdate<String, String>> = bookLoader.betaKeyUpdate

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
                    bookLoader.cancel()
                    bookLoader.load(bookId)
                }
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    fun submitBetaKey(betaKey: String?) {
        bookLoader.cancel()
        bookLoader.submitBetaKey(betaKey)
    }

    // endregion Methods

}
