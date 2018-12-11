/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.*
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book
import io.igist.core.domain.session.AppSessionManager
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
     * The book repository.
     */
    private val bookRepository: BookRepository,

    /**
     * The application repository.
     */
    private val appRepository: AppRepository,

    /**
     * The application [AppSessionManager].
     */
    private val appSessionManager: AppSessionManager

) : ViewModel(),
    OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * A [LiveData] holding the currently-selected book ID.
     */
    private val selectedBookIdData: MutableLiveData<Long> = MutableLiveData()

    /**
     * A [LiveData] holding [Book] updates.
     */
    private val selectedBookUpdateData: LiveData<DataUpdate<Book, Book>> =
        Transformations.switchMap(selectedBookIdData) { bookId -> bookRepository.getBook(bookId) }

    /**
     * A [MediatorLiveData] holding loading information.
     */
    val liveLoading: LiveData<DataUpdate<Int, Boolean>> =
        Transformations.switchMap(selectedBookIdData) { bookId ->
            appRepository.load(bookId)
        }

    // endregion Properties

    // region Constructors

    init {
        // Store the selected book in AppSessionManager
        selectedBookUpdateData.observeForever { update ->
            appSessionManager.book = when (update) {
                is ProgressUpdate -> update.progress.getOrNull(0)
                is SuccessUpdate -> update.result
                else -> null
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (sharedPreferences.contains(PREF_KEY_CURRENT_BOOK_ID)) {
            onSharedPreferenceChanged(sharedPreferences, PREF_KEY_CURRENT_BOOK_ID)
        }
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
     * Sets the new value in [selectedBookIdData].
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            PREF_KEY_CURRENT_BOOK_ID -> {
                selectedBookIdData.value = sharedPreferences.getLong(key, -1)
            }
        }
    }

    // endregion Implemented methods

}
