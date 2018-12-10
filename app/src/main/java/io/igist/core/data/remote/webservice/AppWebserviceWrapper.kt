/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.BuildConfig.API_VERSION
import io.igist.core.BuildConfig.APP_VERSION
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import retrofit2.Call

/**
 * Implementation of [AppWebservice] that allows for default arguments by wrapping another
 * instance ([base]) and passing default arguments to its methods where appropriate.
 */
class AppWebserviceWrapper(private val base: AppWebservice) : AppWebservice {

    override fun api(apiVersion: Int): Call<RemoteApi> = base.api(apiVersion)

    override fun api(): Call<RemoteApi> = base.api(API_VERSION)

    override fun betaKey(betaKey: String): Call<RemoteMessage> = base.betaKey(betaKey)

    override fun content(appVersion: Int): Call<List<RemoteContentList>> = base.content(appVersion)

    override fun content(bookId: Long, appVersion: Int): Call<List<RemoteContentList>> =
        base.content(appVersion)

    override fun content(bookId: Long): Call<List<RemoteContentList>> =
        content(bookId, APP_VERSION)

}
