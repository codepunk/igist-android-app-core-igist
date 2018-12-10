/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig.DEFAULT_BOOK_ID
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.domain.contract.AppRepository
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

) : ViewModel() {

    // region Properties

    /**
     * A [LiveData] holding the currently-selected book ID.
     */
    val bookIdData: MutableLiveData<Long> = MutableLiveData()

    /**
     * A [MediatorLiveData] holding loading information.
     */
    val liveLoading: LiveData<DataUpdate<Int, Boolean>> =
        Transformations.switchMap(bookIdData) { bookId ->
            appRepository.load(bookId)
        }

    // endregion Properties

    // region Constructors

    init {
        val bookId: Long = sharedPreferences.getLong(PREF_KEY_CURRENT_BOOK_ID, DEFAULT_BOOK_ID)
        bookIdData.postValue(bookId)
    }

    // endregion Constructors

}
