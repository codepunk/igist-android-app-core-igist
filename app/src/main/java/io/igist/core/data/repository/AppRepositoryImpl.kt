/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.KEY_DESCRIPTION
import io.igist.core.BuildConfig.PREF_KEY_VALIDATED_BETA_KEY
import io.igist.core.R
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.exception.BadBetaKeyException
import io.igist.core.domain.exception.BetaKeyMissingException
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.IgistMode
import io.igist.core.domain.model.ResultMessage
import io.igist.core.domain.session.AppSessionManager
import retrofit2.Response
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val apiDao: ApiDao,

    private val appWebservice: AppWebservice,

    private val appSessionManager: AppSessionManager

) : AppRepository {

    // region Properties

    private var loadStep: Int = 0

    private var totalLoadSteps: Int = 0

    private var apiTask: ApiTask? = null

    private var apiData: MediatorLiveData<DataUpdate<Api, Api>> = MediatorLiveData()

    private var betaKeyTask: BetaKeyTask? = null

    private var betaKeyData: MediatorLiveData<DataUpdate<String, String>> = MediatorLiveData()

    private var syncContentTask: SyncContentTask? = null

    private var syncContentData: MediatorLiveData<DataUpdate<Int, Void>> = MediatorLiveData()

    private var loadData: MediatorLiveData<DataUpdate<Int, Boolean>> = MediatorLiveData()

    // endregion Properties

    // region Constructors

    init {
        apiData.observeForever { update ->
            when (update) {
                is ProgressUpdate -> {
                    update.progress.getOrNull(0)?.apply {
                        onIgistMode(igistMode)
                    }
                }
                is SuccessUpdate -> {
                    update.result?.apply {
                        onIgistMode(igistMode)
                    }
                }
            }
        }
    }

    // endregion Constructors

    // region Methods

    /**
     * Kicks off the entire loading/onboarding process.
     */
    override fun load(
        apiVersion: Int,
        appVersion: Int,
        alwaysFetchApi: Boolean,
        alwaysValidateBetaKey: Boolean /* TODO I need to pass this along somehow */
    ): LiveData<DataUpdate<Int, Boolean>> {
        // Cancel any existing tasks
        apiTask?.run {
            cancel(true)
            apiData.removeSource(liveData)
        }
        betaKeyTask?.run {
            cancel(true)
            betaKeyData.removeSource(liveData)
        }
        syncContentTask?.run {
            cancel(true)
            syncContentData.removeSource(liveData)
        }

        // Kick off a new Api task
        ApiTask(apiVersion, alwaysFetchApi).apply {
            apiTask = this
            apiData.addSource(liveData) { apiData.value = it }
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        return loadData
    }

    private fun descriptionBundle(description: String): Bundle =
        Bundle().apply {
            putString(KEY_DESCRIPTION, description)
        }

    private fun onIgistMode(igistMode: IgistMode) {
        when {
            igistMode != betaKeyTask?.igistMode -> {
                betaKeyTask?.cancel(true)
                BetaKeyTask(igistMode).apply {
                    betaKeyTask = this
                    betaKeyData.addSource(liveData) { betaKeyData.value = it }
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
            betaKeyTask?.status == AsyncTask.Status.PENDING -> {
                // TODO Unnecessary?
                betaKeyTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }
    }

    // endregion Methods

    // region Nested/inner classes

    @SuppressLint("StaticFieldLeak")
    private inner class ApiTask(val apiVersion: Int, val alwaysFetch: Boolean) :
        DataTaskinator<Void, Api, Api>() {

        override fun onPreExecute() {
            super.onPreExecute()
            loadData.value = ProgressUpdate(
                arrayOf(loadStep, totalLoadSteps),
                descriptionBundle(context.getString(R.string.loading_api))
            )
        }

        override fun doInBackground(vararg params: Void?): ResultUpdate<Api, Api> {
            // Retrieve any cached Api
            val localApi = apiDao.retrieve(apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(api, CancellationException())

            // If an Api was cached, publish it
            api?.run { publishProgress(this) }

            // Optionally fetch latest Api from the network
            if (api == null || alwaysFetch) {
                val remoteApiUpdate: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(apiVersion).toResultUpdate()

                // Check if cancelled or failure
                if (isCancelled) return FailureUpdate(api, CancellationException())
                if (remoteApiUpdate is FailureUpdate) {
                    return FailureUpdate(api, remoteApiUpdate.e, remoteApiUpdate.data)
                }

                remoteApiUpdate.result?.body()?.apply {
                    // Convert & insert remote Api into the local database
                    apiDao.insert(this.toLocalApi())

                    // Re-retrieve the newly-inserted Api from the local database
                    apiDao.retrieve(apiVersion)?.let {
                        api = it.toApi()
                    }
                }
            }

            // Save the Api in the app session manager
            appSessionManager.api = api

            return SuccessUpdate(api)
        }

        override fun onPostExecute(result: ResultUpdate<Api, Api>?) {
            super.onPostExecute(result)
            if (!isCancelled) {
                loadData.value = ProgressUpdate(
                    arrayOf(++loadStep, totalLoadSteps),
                    descriptionBundle(context.getString(R.string.loading_api_finished))
                )
            }
        }

        override fun onCancelled(result: ResultUpdate<Api, Api>?) {
            super.onCancelled(result)
            loadStep = 0
            loadData.value = FailureUpdate(
                false,
                CancellationException(),
                descriptionBundle(context.getString(R.string.loading_cancelled))
            )
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BetaKeyTask(val igistMode: IgistMode, val alwaysValidate: Boolean = true) :
        DataTaskinator<String?, String, String>() {

        override fun doInBackground(vararg params: String?): ResultUpdate<String, String> {
            var betaKey: String? = null
            when (igistMode) {
                IgistMode.REQUIRE_BETA_KEY -> {
                    // The app requires a beta key
                    // Get beta key, either from params or cached in shared preferences
                    betaKey = when {
                        params.isEmpty() ->
                            sharedPreferences.getString(PREF_KEY_VALIDATED_BETA_KEY, null)
                        else -> params[0]
                    }

                    // Check if cancelled
                    if (isCancelled) return FailureUpdate(betaKey, CancellationException())

                    betaKey?.run {
                        if (alwaysValidate) {
                            val betaKeyUpdate: ResultUpdate<Void, Response<RemoteMessage>> =
                                appWebservice.betaKey(betaKey).toResultUpdate()

                            // Check if cancelled or failure
                            if (isCancelled) return FailureUpdate(betaKey, CancellationException())
                            if (betaKeyUpdate is FailureUpdate) {
                                return FailureUpdate(betaKey, betaKeyUpdate.e, betaKeyUpdate.data)
                            }

                            betaKeyUpdate.result?.body()?.apply {
                                val resultMessage = this.resultMessage
                                when (resultMessage) {
                                    ResultMessage.SUCCESS -> {
                                        sharedPreferences.edit()
                                            .putString(PREF_KEY_VALIDATED_BETA_KEY, this@run)
                                            .apply()

                                        // TODO Now we can kick off load task
                                    }
                                    ResultMessage.BAD_KEY -> {
                                        return FailureUpdate(betaKey, BadBetaKeyException())
                                    }
                                }
                            }
                        } else {
                            // TODO Now we can kick off load task
                        }
                    } ?: run {
                        // If we don't have a beta key at this point, that's an error
                        return FailureUpdate(
                            betaKey,
                            BetaKeyMissingException()
                        )
                    }
                }
                else -> {
                    // No beta key is required
                    // Clear any previously-validated beta key from shared preferences
                    sharedPreferences.edit()
                        .remove(PREF_KEY_VALIDATED_BETA_KEY)
                        .apply()

                    // Check if cancelled


                    // TODO Now we can kick off load task
                }
            }

            // TODO If we made it here, we can kick off load task. But we should probably catch
            // it in observer

            return SuccessUpdate(betaKey)
        }

    }

    @SuppressLint("StaticFieldLeak")
    private inner class SyncContentTask(val appVersion: Int) :
        DataTaskinator<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Void> {
            TODO("not implemented")
        }

    }

    // endregion Nested/inner classes

}
