/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.*
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.IgistMode
import io.igist.core.domain.model.ResultMessage
import retrofit2.Response
import java.lang.IllegalStateException
import javax.inject.Inject

class AppRepositoryImplOld @Inject constructor(

    private val sharedPreferences: SharedPreferences,

    private val appDao: ApiDao,

    private val appWebservice: AppWebservice

) : AppRepository {

    // region Properties

    private var api: Api? = null
        set(value) {
            when (value?.igistMode) {
                field?.igistMode -> {
                    /* Igist mode is not changing; no action is needed */
                    Log.d(
                        "AppRepositoryImplOld",
                        "igistMode=${value?.igistMode}: Igist mode is not changing; no action needed"
                    )
                }
                IgistMode.NONE -> {
                    Log.d(
                        "AppRepositoryImplOld",
                        "igistMode=${value.igistMode}: Stop asking for beta key (and remove from shared pref)"
                    )
                    Log.d(
                        "AppRepositoryImplOld",
                        "igistMode=${value.igistMode}: Start loading content"
                    )
                }
                IgistMode.REQUIRE_BETA_KEY -> {
                    // Do check beta key task?
                    Log.d(
                        "AppRepositoryImplOld",
                        "igistMode=${value.igistMode}: Stop loading content"
                    )
//                    authorizeBetaKeyTask?.cancel(true)
                    authorizeBetaKey("igist" /* TODO TEMP */, true)

                    Log.d(
                        "AppRepositoryImplOld",
                        "igistMode=${value.igistMode}: Ask for beta key"
                    )
                }
                null -> {

                }
            }
            field = value
        }

    private var getApiTask: GetApiTask? = null

    private var authorizeBetaKeyTask: AuthorizeBetaKeyTask? = null

    private var loadContentTask: LoadContentTask? = null

    // endregion Properties

    // region Implemented methods

    /* override */ fun getApi(
        apiVersion: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Api, Api>> {
        loadContentTask?.cancel(true)
        // TODO Cancel beta key task?
        getApiTask?.cancel(true)
        return GetApiTask(appDao, appWebservice, alwaysFetch).apply {
            getApiTask = this
        }.executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            apiVersion
        ).apply {
            observeForever { onApi(it) }
        }
    }

    /* override */ fun authorizeBetaKey(
        betaKey: String?,
        alwaysAuthorize: Boolean
    ): LiveData<DataUpdate<String, String>> {
        loadContentTask?.cancel(true)
        authorizeBetaKeyTask?.cancel(true)
        return AuthorizeBetaKeyTask(sharedPreferences, appWebservice, alwaysAuthorize).apply {
            authorizeBetaKeyTask = this
        }.executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            betaKey
        ).apply {
            observeForever { onBetaKey(it) }
        }
    }

    override fun load(
        apiVersion: Int,
        appVersion: Int,
        alwaysFetchApi: Boolean,
        alwaysValidateBetaKey: Boolean
    ): LiveData<DataUpdate<Int, Boolean>> {
        TODO("not implemented")
    }

    // endregion Implemented methods

    // region Methods

    private fun onApi(update: DataUpdate<Api, Api>) {
        when {
            update is SuccessUpdate -> api = update.result
            update is ProgressUpdate && update.progress.isNotEmpty() ->
                api = update.progress[0]
            update is FailureUpdate -> {

            }
        }
    }

    private fun onBetaKey(update: DataUpdate<String, String>) {
        Log.d("AppRepositoryImplOld", "update=$update")
    }

    // endregion Methods

    // region Nested/inner classes

    private class GetApiTask(

        private val appDao: ApiDao,

        private val appWebservice: AppWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Int, Api, Api>() {

        override fun doInBackground(vararg params: Int?): ResultUpdate<Api, Api> {
            // TODO Check for cancel!!

            // Try to get Api from local database
            val apiVersion = params.getOrNull(0) ?: API_VERSION
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

    private class AuthorizeBetaKeyTask(

        private val sharedPreferences: SharedPreferences,

        private val appWebservice: AppWebservice,

        private val alwaysValidate: Boolean = true

    ) : DataTaskinator<String?, String, String>() {

        override fun doInBackground(vararg params: String?): ResultUpdate<String, String> {
            // TODO Check for cancel!!

            // Check shared preferences for already-existing authorized beta key
            val authorizedBetaKey: String? =
                sharedPreferences.getString(PREF_KEY_VALIDATED_BETA_KEY, null)

            val enteredBetaKey: String? = params.getOrNull(0) ?: authorizedBetaKey

            // If we got a previously-authorized beta key, publish it
            if (authorizedBetaKey != null) {
                publishProgress(authorizedBetaKey)
            }

            // Optionally authorize the beta key on the server.
            if (enteredBetaKey == null) {
                return FailureUpdate(
                    enteredBetaKey,
                    IllegalStateException("No authorized beta key and no beta key supplied")
                )
            }

            // TODO Figure out when to validate given authorizedBetaKey, enteredBetaKey, etc.
            if (enteredBetaKey != null || alwaysValidate) {
                val betaKeyUpdate: ResultUpdate<Void, Response<RemoteMessage>> =
                    appWebservice.betaKey(enteredBetaKey).toResultUpdate()
                val remoteMessage: RemoteMessage? = when (betaKeyUpdate) {
                    is FailureUpdate -> return FailureUpdate(
                        enteredBetaKey,
                        betaKeyUpdate.e,
                        betaKeyUpdate.data
                    )
                    else -> betaKeyUpdate.result?.body()
                }

                remoteMessage?.apply {
                    val resultMessage = this.resultMessage
                    when (resultMessage) {
                        ResultMessage.BAD_KEY -> {
                            Log.d("AppRepositoryImplOld", "BAD_KEY")
                        }
                        ResultMessage.SUCCESS -> {
                            Log.d("AppRepositoryImplOld", "SUCCESS")
                        }
                    }
                }
            }

            return SuccessUpdate(enteredBetaKey)
        }

    }

    private class LoadContentTask : DataTaskinator<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): ResultUpdate<Void, Void> {
            TODO("not implemented")
        }

    }

    // endregion Nested/inner classes

}
