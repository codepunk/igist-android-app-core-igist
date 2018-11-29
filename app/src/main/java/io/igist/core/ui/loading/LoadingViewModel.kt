/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.annotation.SuppressLint
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.igist.core.data.task.DataTask
import com.igist.core.data.task.DataUpdate
import io.igist.core.data.model.Api
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.data.task.toDataUpdate
import javax.inject.Inject

class LoadingViewModel @Inject constructor(

    /**
     * The app webservice.
     */
    private val appWebservice: AppWebservice

) : ViewModel() {

    // region Properties

    /**
     * A [MediatorLiveData] holding the application [Api] information.
     */
    val apiDataUpdate = MediatorLiveData<DataUpdate<Void, Api>>()

    /**
     * A [MediatorLiveData] for tracking the loading process.
     */
    val loadingDataUpdate = MediatorLiveData<DataUpdate<Int, Void>>()

    // endregion Properties

    // region Methods

    @SuppressLint("StaticFieldLeak")
    fun getApi() {
        val task = object : DataTask<Void, Void, Api>() {
            override fun generateUpdate(vararg params: Void?): DataUpdate<Void, Api> =
                appWebservice.api().toDataUpdate()
        }
        apiDataUpdate.addSource(task.fetchOnExecutor()) { apiDataUpdate.value = it }
    }

    fun load() {
        // TODO This will be a massive and complicated method that will perform all loading
    }

    // endregion Methods

}
