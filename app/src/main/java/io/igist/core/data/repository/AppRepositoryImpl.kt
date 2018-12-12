/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import retrofit2.Response
import java.util.concurrent.CancellationException

class AppRepositoryImpl(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val apiDao: ApiDao,

    private val appWebservice: AppWebservice

) : AppRepository {

    // region Implemented methods

    override fun getApi(
        bookId: Long,
        apiVersion: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Api, Api>> =
        ApiTask(apiDao, appWebservice, alwaysFetch).executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            bookId,
            apiVersion
        )

    // endregion Implemented methods

    // region Nested/inner classes

    /**
     * [DataTaskinator] that retrieves API information.
     */
    private class ApiTask(

        private val apiDao: ApiDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Any, Api, Api>() {

        override fun doInBackground(vararg params: Any?): ResultUpdate<Api, Api> {
            val bookId: Long = params.getOrNull(0) as Long?
                ?: throw IllegalArgumentException("No book ID passed to ApiTask")

            val apiVersion: Int = params.getOrNull(1) as Int?
                ?: throw IllegalArgumentException("No API version passed to ApiTask")

            // Retrieve any cached api
            val localApi = apiDao.retrieve(bookId, apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Fetch the latest API info
            if (api == null || alwaysFetch) {
                val remoteApiUpdate: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(bookId, apiVersion).toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(api, CancellationException(), data)
                    remoteApiUpdate is FailureUpdate -> {
                        // TODO addDescription(context.getString(R.string.loading_unknown_error))
                        return FailureUpdate(api, remoteApiUpdate.e, data)
                    }
                }

                remoteApiUpdate.result?.body()?.apply {
                    // Convert & insert remote Api into the local database
                    apiDao.insert(this.toLocalApi(bookId))

                    // Re-retrieve the newly-inserted Api from the local database
                    apiDao.retrieve(bookId, apiVersion)?.let {
                        api = it.toApi()
                    }
                }
            }

            // TODO addDescription(context.getString(R.string.api_loaded))
            return SuccessUpdate(api)
        }

    }

    // endregion Nested/inner classes

}
