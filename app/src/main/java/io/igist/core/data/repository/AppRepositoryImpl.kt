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
import io.igist.core.BuildConfig
import io.igist.core.BuildConfig.KEY_DESCRIPTION
import io.igist.core.BuildConfig.PREF_KEY_VALIDATED_BETA_KEY
import io.igist.core.R
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.exception.BadBetaKeyException
import io.igist.core.domain.exception.BetaKeyRequiredException
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.ContentList
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

    private var contentTask: ContentTask? = null

    private var contentData: MediatorLiveData<DataUpdate<List<ContentList>, List<ContentList>>> =
        MediatorLiveData()

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

        betaKeyData.observeForever { update ->
            when (update) {
                is SuccessUpdate -> {
                    contentTask?.cancel(true)
                    ContentTask(BuildConfig.APP_VERSION /* TODO TEMP */).apply {
                        contentTask = this
                        contentData.addSource(liveData) { contentData.value = it }
                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
        loadStep = 0
        totalLoadSteps = 0

        // Cancel any existing tasks
        apiTask?.run {
            cancel(true)
            apiData.removeSource(liveData)
        }
        betaKeyTask?.run {
            cancel(true)
            betaKeyData.removeSource(liveData)
        }
        contentTask?.run {
            cancel(true)
            contentData.removeSource(liveData)
        }

        // Kick off a new Api task
        ApiTask(apiVersion, alwaysFetchApi).apply {
            apiTask = this
            apiData.addSource(liveData) { apiData.value = it }
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        return loadData
    }

    private fun onIgistMode(igistMode: IgistMode) {
        when {
            igistMode != betaKeyTask?.igistMode -> {
                betaKeyTask?.cancel(true)
                // TODO Cancel contentTask too?
                BetaKeyTask(igistMode).apply {
                    betaKeyTask = this
                    betaKeyData.addSource(liveData) { betaKeyData.value = it }
                    executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        "igist" /* TODO TEMP */
                    )
                }
            }
            betaKeyTask?.status == AsyncTask.Status.PENDING -> {
                // TODO Unnecessary?
                betaKeyTask?.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    "igist" /* TODO TEMP */
                )
            }
        }
    }

    // endregion Methods

    // region Nested/inner classes

    private abstract inner class AbsTask<Params, Progress, Result>(
        data: Bundle? = null
    ) : DataTaskinator<Params, Progress, Result>(data) {

        override fun onPreExecute() {
            super.onPreExecute()
            loadData.value = ProgressUpdate(arrayOf(++loadStep, totalLoadSteps), data)
        }

        override fun onPostExecute(result: ResultUpdate<Progress, Result>?) {
            super.onPostExecute(result)
            when (result) {
                is SuccessUpdate -> loadData.value =
                        ProgressUpdate(arrayOf(++loadStep, totalLoadSteps), result.data)
                is FailureUpdate -> loadData.value =
                        FailureUpdate(false, result.e, result.data)
            }
        }

        override fun onCancelled(result: ResultUpdate<Progress, Result>?) {
            super.onCancelled(result)
            addDescription(context.getString(R.string.loading_cancelled))
            loadData.value = FailureUpdate(false, CancellationException(), data)
        }

        protected fun addDescription(description: String) {
            data.putString(KEY_DESCRIPTION, description)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ApiTask(val apiVersion: Int, val alwaysFetch: Boolean) :
        AbsTask<Void, Api, Api>() {

        init {
            addDescription(context.getString(R.string.loading_api))
        }

        override fun doInBackground(vararg params: Void?): ResultUpdate<Api, Api> {
            // Retrieve any cached Api
            val localApi = apiDao.retrieve(apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(api, CancellationException(), data)

            // If an Api was cached, publish it
            api?.run { publishProgress(this) }

            // Optionally fetch latest Api from the network
            if (api == null || alwaysFetch) {
                val remoteApiUpdate: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(apiVersion).toResultUpdate(data)

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(api, CancellationException(), data)
                    remoteApiUpdate is FailureUpdate -> {
                        addDescription(context.getString(R.string.loading_unknown_error))
                        return FailureUpdate(api, remoteApiUpdate.e, data)
                    }
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

            addDescription(context.getString(R.string.api_loaded))
            return SuccessUpdate(api, data)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BetaKeyTask(val igistMode: IgistMode, val alwaysValidate: Boolean = true) :
        AbsTask<String?, String, String>() {

        init {
            addDescription(context.getString(R.string.loading_validating_beta_key))
        }

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

                    when {
                        betaKey.isNullOrEmpty() -> {
                            // This is an error
                            addDescription(context.getString(R.string.loading_beta_key_required))
                            return FailureUpdate(betaKey, BetaKeyRequiredException(), data)
                        }
                        alwaysValidate -> {
                            // Validate the beta key
                            val betaKeyUpdate: ResultUpdate<Void, Response<RemoteMessage>> =
                                appWebservice.betaKey(betaKey).toResultUpdate()

                            // Check if cancelled or failure
                            when {
                                isCancelled ->
                                    return FailureUpdate(betaKey, CancellationException())
                                betaKeyUpdate is FailureUpdate -> {
                                    addDescription(
                                        context.getString(R.string.loading_unknown_error)
                                    )
                                    return FailureUpdate(betaKey, betaKeyUpdate.e, data)
                                }
                            }

                            betaKeyUpdate.result?.body()?.apply {
                                val resultMessage = this.resultMessage
                                when (resultMessage) {
                                    ResultMessage.SUCCESS -> {
                                        sharedPreferences.edit()
                                            .putString(PREF_KEY_VALIDATED_BETA_KEY, betaKey)
                                            .apply()
                                    }
                                    ResultMessage.BAD_KEY -> {
                                        addDescription(
                                            context.getString(R.string.loading_beta_key_invalid)
                                        )
                                        return FailureUpdate(betaKey, BadBetaKeyException(), data)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // No beta key is required; clear any previously-validated beta key from
                    // shared preferences
                    sharedPreferences.edit()
                        .remove(PREF_KEY_VALIDATED_BETA_KEY)
                        .apply()

                    // Check if cancelled
                    if (isCancelled) return FailureUpdate(betaKey, CancellationException())
                }
            }

            addDescription(context.getString(R.string.loading_beta_key_validated))
            return SuccessUpdate(betaKey, data)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ContentTask(val appVersion: Int, val alwaysFetch: Boolean = true) :
        AbsTask<Void, List<ContentList>, List<ContentList>>() {

        init {
            addDescription(context.getString(R.string.loading_content_metadata))
        }

        override fun doInBackground(vararg params: Void?):
                ResultUpdate<List<ContentList>, List<ContentList>> {
            // Retrieve any cached content
            // TODO
            val contentLists: List<ContentList>? = null

            // Check if cancelled
            if (isCancelled) return FailureUpdate(null, CancellationException(), data)

            // Optionally fetch latest ContentList from the network
            if (contentLists == null || alwaysFetch) {
                val remoteContentListUpdate: ResultUpdate<Void, Response<List<RemoteContentList>>> =
                    appWebservice.content(BuildConfig.DEFAULT_BOOK_ID.toLong(), appVersion)
                        .toResultUpdate(data)

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(contentLists, CancellationException(), data)
                    remoteContentListUpdate is FailureUpdate -> {
                        addDescription(context.getString(R.string.loading_unknown_error))
                        return FailureUpdate(contentLists, remoteContentListUpdate.e, data)
                    }
                }

                remoteContentListUpdate.result?.body()?.apply {
                    // Convert & insert remote Api into the local database
                    // apiDao.insert(this.toLocalApi())

                    // Re-retrieve the newly-inserted Api from the local database
                    //apiDao.retrieve(apiVersion)?.let {
                    //    api = it.toApi()
                    //}
                }
            }

            addDescription(context.getString(R.string.content_metadata_loaded))
            return SuccessUpdate(contentLists, data)
        }

    }

    // endregion Nested/inner classes

}
