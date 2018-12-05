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
import io.igist.core.data.mapper.ApiLocalToDomainMapper
import io.igist.core.data.mapper.ApiRemoteToLocalMapper
import io.igist.core.data.remote.entity.ApiRemote
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import retrofit2.Response
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(

    private val appDao: ApiDao,

    private val appWebservice: AppWebservice,

    private val apiLocalToDomainMapper: ApiLocalToDomainMapper,

    private val apiRemoteToLocalMapper: ApiRemoteToLocalMapper

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
        apiLocalToDomainMapper,
        apiRemoteToLocalMapper,
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

        private val apiLocalToDomainMapper: ApiLocalToDomainMapper,

        private val apiRemoteToLocalMapper: ApiRemoteToLocalMapper,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Int, Api, Api>() {

        override fun doInBackground(vararg params: Int?): ResultUpdate<Api, Api> {
            // Try to get Api from local database
            val apiVersion = params.getOrNull(0) ?: BuildConfig.API_VERSION
            val apiLocal = appDao.retrieve(apiVersion)
            var api: Api? = apiLocal?.let { apiLocalToDomainMapper.map(apiLocal) }

            // If we got a local Api, publish it
            if (api != null) {
                publishProgress(api)
            }

            // Optionally fetch latest Api from the network
            if (apiLocal == null || alwaysFetch) {
                val apiRemoteUpdate: ResultUpdate<Void, Response<ApiRemote>> =
                    appWebservice.api(apiVersion).toResultUpdate()
                val apiRemote = when (apiRemoteUpdate) {
                    is FailureUpdate -> return FailureUpdate(
                        api,
                        apiRemoteUpdate.e,
                        apiRemoteUpdate.data
                    )
                    else -> apiRemoteUpdate.result?.body()
                }

                if (apiRemote != null) {
                    // Convert & insert remote Api into the local database
                    appDao.insert(apiRemoteToLocalMapper.map(apiRemote))

                    // Re-retrieve the newly-inserted Api from the local database
                    appDao.retrieve(apiVersion)?.let {
                        api = apiLocalToDomainMapper.map(it)
                    }
                }
            }

            return SuccessUpdate(api)
        }

    }

    // endregion Nested/inner classes

}
