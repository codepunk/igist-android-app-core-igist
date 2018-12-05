/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.entity.ApiEntity
import io.igist.core.data.model.Api
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository for fetching app-level information and implementing the onboarding/loading process.
 */
@Singleton
class LoadingRepository @Inject constructor(

    private val appDao: ApiDao,

    private val appWebservice: AppWebservice

) {

    // region Methods

    /**
     * A method for getting api data, wrapped in [DataUpdate]/[LiveData].
     */
    @SuppressLint("StaticFieldLeak")
    fun getApiUpdateData(apiVersion: Int): LiveData<DataUpdate<Api, Api>> {
        val task = object : DataTaskinator<Void, Api, Api>() {
            override fun doInBackground(vararg params: Void): ResultUpdate<Api, Api> {
                // Step 1: Attempt to get Api from local database
                val localApi: Api? = appDao.retrieve(apiVersion)?.let { Api(it) }

                // Step 2: If we have a local Api, publish progress
                if (localApi != null) {
                    publishProgress(localApi)
                }

                // Step 3: Retrieve latest Api from network
                val apiUpdate: ResultUpdate<Void, Response<Api>> =
                    appWebservice.api(apiVersion).toResultUpdate()
                val remoteApi = apiUpdate.result?.body()
                if (apiUpdate is FailureUpdate) {
                    return FailureUpdate(remoteApi, apiUpdate.e)
                }

                // Step 4: Insert into local db and then retrieve it out
                val api: Api? =
                    remoteApi?.run {
                        appDao.insert(ApiEntity(this))
                        appDao.retrieve(apiVersion)?.let { Api(it) }
                    }

                // Return the newly-inserted Api as the "one source of truth"
                return SuccessUpdate(api)
            }
        }
        return task.fetchOnExecutor()
    }

    // endregion Methods

}
