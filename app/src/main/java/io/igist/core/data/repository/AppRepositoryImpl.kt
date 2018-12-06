/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import retrofit2.Response
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(

    private val appDao: ApiDao,

    private val appWebservice: AppWebservice

) : AppRepository {

    // region Properties

    private var getApiTask: GetApiTask? = null

    // endregion Properties

    // region Methods

    override fun getApi(
        apiVersion: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Api, Api>> = GetApiTask(
        appDao,
        appWebservice,
        alwaysFetch
    ).apply {
        getApiTask?.cancel(true)
        getApiTask = this
    }.executeOnExecutorAsLiveData(
        AsyncTask.THREAD_POOL_EXECUTOR,
        BuildConfig.API_VERSION
    )

    // endregion Methods

    // region Nested/inner classes

    private class GetApiTask(

        private val appDao: ApiDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Int, Api, Api>() {

        override fun doInBackground(vararg params: Int?): ResultUpdate<Api, Api> {
            // Try to get Api from local database
            val apiVersion = params.getOrNull(0) ?: BuildConfig.API_VERSION
            val localApi = appDao.retrieve(apiVersion)
            var api = localApi.toApiOrNull()

            // If we got a local Api, publish it
            if (api != null) {
                publishProgress(api)
            }

            // Optionally fetch latest Api from the network
            if (localApi == null || alwaysFetch) {
                val remoteApiUpdate: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(apiVersion).toResultUpdate()
                val remoteApi = when (remoteApiUpdate) {
                    is FailureUpdate -> return FailureUpdate(
                        api,
                        remoteApiUpdate.e,
                        remoteApiUpdate.data
                    )
                    else -> remoteApiUpdate.result?.body()
                }

                remoteApi?.run {
                    // Convert & insert remote Api into the local database
                    appDao.insert(this.toLocalApi())

                    // Re-retrieve the newly-inserted Api from the local database
                    appDao.retrieve(apiVersion)?.let {
                        api = it.toApi()
                    }
                }
            }

            return SuccessUpdate(api)
        }

    }

    // endregion Nested/inner classes

}
