/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book
import javax.inject.Inject

/**
 * A [ViewModel] that handles book selection.
 */
class SelectBookViewModel @Inject constructor(

    /**
     * The application shared preferences.
     */
    private val sharedPreferences: SharedPreferences,

    /**
     * The book repository.
     */
    bookRepository: BookRepository

) : ViewModel(),
    OnSharedPreferenceChangeListener {

    // region Properties

    /**
     * A [LiveData] holding the selected book ID.
     */
    val bookIdData: MutableLiveData<Long> = MutableLiveData()

    /**
     * A [LiveData] holding the list of available books.
     */
    val books: LiveData<DataUpdate<List<Book>, List<Book>>>

    // endregion Properties

    // region Constructors

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        books = bookRepository.getBooks()
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

    // endregion Methods

}
