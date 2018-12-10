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
import com.codepunk.doofenschmirtz.util.taskinator.DataTaskinator
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate
import io.igist.core.BuildConfig.*
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.Book
import io.igist.core.domain.session.AppSessionManager
import java.util.concurrent.CancellationException

class AppRepositoryImpl2(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val apiDao: ApiDao,

    private val appWebservice: AppWebservice,

    private val appSessionManager: AppSessionManager

) : AppRepository {

    val loadData: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    private var apiTask: ApiTask? = null

    override fun load(
        bookId: Long,
        apiVersion: Int,
        appVersion: Int,
        alwaysFetchApi: Boolean,
        alwaysValidateBetaKey: Boolean
    ): LiveData<DataUpdate<Int, Boolean>> {

        apiTask?.cancel(true)

        val arguments = Bundle().apply {
            putLong(KEY_BOOK_ID, bookId)
            putInt(KEY_API_VERSION, apiVersion)
            putInt(KEY_APP_VERSION, appVersion)
        }

        ApiTask(loadData, apiDao).apply {
            apiTask = this
            executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, arguments)
        }

        return loadData

    }

    // region Nested/inner classes

    class ApiTask(

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
                ?: throw IllegalArgumentException("ApiTask requires a Bundle argument")
            val bookId = when {
                arguments.containsKey(KEY_BOOK_ID) -> arguments.getLong(KEY_BOOK_ID)
                else -> throw IllegalArgumentException(
                    "ApiTask requires KEY_BOOK_ID to be set in arguments"
                )
            }
            val apiVersion = when {
                arguments.containsKey(KEY_API_VERSION) -> arguments.getInt(KEY_API_VERSION)
                else -> throw IllegalArgumentException(
                    "ApiTask requires KEY_API_VERSION to be set in arguments"
                )
            }
            val appVersion = when {
                arguments.containsKey(KEY_APP_VERSION) -> arguments.getInt(KEY_APP_VERSION)
                else -> throw IllegalArgumentException(
                    "ApiTask requires KEY_APP_VERSION to be set in arguments"
                )
            }

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
