/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.igist.core.data.task.DataTask
import com.igist.core.data.task.DataUpdate
import io.igist.core.data.model.Api
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.data.task.toDataUpdate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository for fetching app-level information and implementing the onboarding/loading process.
 */
@Singleton
class LoadingRepository @Inject constructor(
    private val appWebservice: AppWebservice
) {

    // region Methods

    /**
     * A method for getting api data, wrapped in [DataUpdate]/[LiveData].
     */
    @SuppressLint("StaticFieldLeak")
    fun getApiUpdateData(apiVersion: Int): LiveData<DataUpdate<Int, Api>> {
        val task = object : DataTask<Void, Int, Api>() {
            override fun generateUpdate(vararg params: Void?): DataUpdate<Int, Api> =
                appWebservice.api(apiVersion).toDataUpdate()
        }
        return task.fetchOnExecutor()
    }

    // endregion Methods

}
