/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import androidx.lifecycle.*
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.data.model.Api
import io.igist.core.data.repository.LoadingRepository
import javax.inject.Inject

class LoadingViewModel @Inject constructor(

    /**
     * The loading repository.
     */
    private val loadingRepository: LoadingRepository

) : ViewModel() {

    // region Properties

    /**
     * A backing [LiveData] holding the API version. Used to trigger other loading processes.
     */
    private val apiVersionData = MutableLiveData<Int>()

    /**
     * A [MediatorLiveData] holding the application [Api] information.
     */
    val apiUpdateData: LiveData<DataUpdate<Api, Api>> =
        Transformations.switchMap(apiVersionData) { apiVersion ->
            loadingRepository.getApiUpdateData(apiVersion)
        }

    /**
     * The API version. Setting this value will kick off the logic in [apiUpdateData].
     */
    var apiVersion: Int = -1
        set(value) {
            field = value
            apiVersionData.value = field
        }

    @Suppress("UNUSED")
    fun load() {
        // TODO This will be a massive and complicated method that will perform all loading
    }

    // endregion Methods

}
