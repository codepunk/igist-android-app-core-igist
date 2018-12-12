/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import retrofit2.Call

/**
 * Implementation of [AppWebservice] that allows for default arguments by wrapping another
 * instance ([base]) and passing default arguments to its methods where appropriate.
 */
class AppWebserviceWrapper(private val base: AppWebservice) : AppWebservice {

    /**
     * Gets API information for the given [bookId] and [apiVersion]. As the app currently
     * does not support multiple books, we'll just ditch the book ID for now.
     */
    override fun api(bookId: Long, apiVersion: Int): Call<RemoteApi> = api(apiVersion)

    override fun api(apiVersion: Int): Call<RemoteApi> = base.api(apiVersion)

    override fun betaKey(betaKey: String): Call<RemoteMessage> = base.betaKey(betaKey)

    override fun content(appVersion: Int): Call<List<RemoteContentList>> = base.content(appVersion)

    override fun content(bookId: Long, appVersion: Int): Call<List<RemoteContentList>> =
        base.content(appVersion)

}
