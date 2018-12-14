/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.SharedPreferences
import android.os.AsyncTask.THREAD_POOL_EXECUTOR
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.PREF_KEY_VERIFIED_BETA_KEY
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.mapper.toApi
import io.igist.core.data.mapper.toApiOrNull
import io.igist.core.data.mapper.toLocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteMessage
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.BookMode
import io.igist.core.domain.model.BookMode.DEFAULT
import io.igist.core.domain.model.BookMode.REQUIRE_BETA_KEY
import io.igist.core.domain.model.ResultMessage
import retrofit2.Response
import java.util.concurrent.CancellationException

/**
 * A Retrofit-based implementation of [AppRepository].
 */
class AppRepositoryImpl(

    private val apiDao: ApiDao,

    private val appWebservice: AppWebservice,

    private val sharedPreferences: SharedPreferences

) : AppRepository {

    // region Properties

    private var apiTask: ApiTask? = null

    private var betaKeyTask: BetaKeyTask? = null

    // endregion Properties

    // region Implemented methods

    override fun getApi(
        bookId: Long,
        apiVersion: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Api, Api>> {
        apiTask?.cancel(true)
        betaKeyTask?.cancel(true)
        return ApiTask(
            apiDao,
            appWebservice,
            alwaysFetch
        ).apply {
            apiTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookId,
            apiVersion
        )
    }

    override fun checkBetaKey(
        bookMode: BookMode,
        betaKey: String?,
        alwaysVerify: Boolean
    ): LiveData<DataUpdate<String, String>> {
        betaKeyTask?.cancel(true)
        return BetaKeyTask(
            appWebservice,
            sharedPreferences,
            alwaysVerify
        ).apply {
            betaKeyTask = this
        }.executeOnExecutorAsLiveData(
            THREAD_POOL_EXECUTOR,
            bookMode,
            betaKey
        )
    }

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
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0) as? Long?
                ?: throw IllegalArgumentException("No book ID passed to ApiTask")
            val apiVersion: Int = params.getOrNull(1) as? Int?
                ?: throw IllegalArgumentException("No API version passed to ApiTask")

            // Retrieve any cached api
            val localApi = apiDao.retrieve(bookId, apiVersion)
            var api: Api? = localApi.toApiOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(api, CancellationException(), data)

            // Fetch the latest API info
            if (api == null || alwaysFetch) {
                // If we have a cached Api, publish it
                if (api != null) publishProgress(api)

                val update: ResultUpdate<Void, Response<RemoteApi>> =
                    appWebservice.api(bookId, apiVersion).toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(api, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(api, update.e, data)
                }

                update.result?.body()?.apply {
                    // Convert & insert remote Api into the local database
                    apiDao.insert(this.toLocalApi(bookId))

                    // Re-retrieve the newly-inserted Api from the local database
                    apiDao.retrieve(bookId, apiVersion)?.let {
                        api = it.toApi()
                    }
                }
            }

            return SuccessUpdate(api)
        }

    }

    /**
     * [DataTaskinator] that checks a beta key (entered or cached) against the server.
     */
    private class BetaKeyTask(

        private val appWebservice: AppWebservice,

        private val sharedPreferences: SharedPreferences,

        private val alwaysVerify: Boolean = true

    ) : DataTaskinator<Any?, String, String>() {

        override fun doInBackground(vararg params: Any?): ResultUpdate<String, String> {
            // Extract arguments from params
            val bookMode: BookMode = params.getOrNull(0) as? BookMode?
                ?: throw IllegalArgumentException("No BookMode passed to BetaKeyTask")
            var betaKey: String? = params.getOrNull(1) as? String?

            // Check if cancelled
            if (isCancelled) return FailureUpdate(betaKey, CancellationException(), data)

            // Verify the beta key if the book mode requires it
            when (bookMode) {
                DEFAULT -> {
                    // No beta key is required. Remove any previously-verified beta key from
                    // shared preferences and return success with null beta key value
                    sharedPreferences.edit().remove(PREF_KEY_VERIFIED_BETA_KEY).apply()
                    betaKey = null
                }
                REQUIRE_BETA_KEY -> {
                    // If we have a beta key (whether user-entered or previously-validated),
                    // publish it
                    if (betaKey != null) publishProgress(betaKey)

                    when {
                        betaKey == null -> return FailureUpdate(
                            betaKey,
                            IgistException(ResultMessage.BETA_KEY_REQUIRED),
                            data
                        )
                        alwaysVerify -> {
                            val update: ResultUpdate<Void, Response<RemoteMessage>> =
                                appWebservice.betaKey(
                                    if (betaKey.isEmpty()) " " else betaKey // Avoid empty string
                                ).toResultUpdate()

                            // Check if cancelled or failure
                            when {
                                isCancelled -> return FailureUpdate(
                                    betaKey,
                                    CancellationException(),
                                    data
                                )
                                update is FailureUpdate ->
                                    return FailureUpdate(betaKey, update.e, data)
                            }

                            // Check the result message
                            val message = update.result?.body()?.resultMessage
                                ?: ResultMessage.UnknownResultMessage()
                            when (message) {
                                ResultMessage.SUCCESS -> sharedPreferences.edit()
                                    .putString(PREF_KEY_VERIFIED_BETA_KEY, betaKey)
                                    .apply()
                                else -> return FailureUpdate(
                                    null,
                                    IgistException(message),
                                    data
                                )
                            }
                        }
                    }
                }
            }

            return SuccessUpdate(betaKey)
        }

    }

    // endregion Nested/inner classes

}
