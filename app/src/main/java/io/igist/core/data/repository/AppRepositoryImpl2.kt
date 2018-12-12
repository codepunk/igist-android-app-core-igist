/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.*
import io.igist.core.R
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

class AppRepositoryImpl2(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val apiDao: ApiDao,

    private val appWebservice: AppWebservice

) : AppRepository {

    val loadData: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    private var apiTaskOld: ApiTaskOld? = null

    override fun getApi(bookId: Long, apiVersion: Int): LiveData<DataUpdate<Api, Api>> =
        ApiTask(apiDao, appWebservice).executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            bookId,
            apiVersion
        )

    /*
    fun load(
        bookId: Long,
        apiVersion: Int,
        appVersion: Int,
        alwaysFetchApi: Boolean,
        alwaysValidateBetaKey: Boolean
    ): LiveData<DataUpdate<Int, Boolean>> {

        apiTaskOld?.cancel(true)

        val arguments = Bundle().apply {
            putLong(KEY_BOOK_ID, bookId)
            putInt(KEY_API_VERSION, apiVersion)
            putInt(KEY_APP_VERSION, appVersion)
        }

        ApiTaskOld(loadData, apiDao).apply {
            apiTaskOld = this
            executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, arguments)
        }

        return loadData

    }
    */

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
                ?: throw IllegalArgumentException("No API verison passed to ApiTask")

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

    class ApiTaskOld(

        private val loadData: MediatorLiveData<DataUpdate<Int, Boolean>>,

        private val apiDao: ApiDao,

        data: Bundle? = null

    ) : DataTaskinator<Bundle, Api, Api>(data) {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Bundle?): ResultUpdate<Api, Api> {
            // Extract arguments
            val arguments: Bundle = params.getOrNull(0)
                ?: throw IllegalArgumentException("ApiTaskOld requires a Bundle argument")
            val bookId = when {
                arguments.containsKey(KEY_BOOK_ID) -> arguments.getLong(KEY_BOOK_ID)
                else -> throw IllegalArgumentException(
                    "ApiTaskOld requires KEY_BOOK_ID to be set in arguments"
                )
            }
            val apiVersion = 0
            val appVersion = 1

            // Retrieve any cached Api
            val localApi = apiDao.retrieve(bookId, apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(api, CancellationException())

            // If an Api was cached, publish it
            api?.run { publishProgress(this) }

            return FailureUpdate()
        }

        override fun onPostExecute(result: ResultUpdate<Api, Api>?) {
            super.onPostExecute(result)

            loadData.removeSource(liveData)
        }

        override fun onCancelled(result: ResultUpdate<Api, Api>?) {
            super.onCancelled(result)

            loadData.removeSource(liveData)
        }
    }

    // endregion Nested/inner classes
}
