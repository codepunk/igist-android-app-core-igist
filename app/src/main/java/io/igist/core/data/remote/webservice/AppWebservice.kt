/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.data.remote.HEADER_ACCEPT_APPLICATION_JSON
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteMessage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * A webservice that defines API-related calls.
 */
@Suppress("UNUSED")
interface AppWebservice {

    /**
     * Gets the app API-level information.
     */
    @GET("api/{apiVersion}")
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun api(@Path(value = "apiVersion") apiVersion: Int): Call<RemoteApi>

    /**
     * Checks a beta key against the server.
     */
    @GET("api/beta_key/{betaKey}")
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun betaKey(@Path(value = "betaKey") betaKey: String): Call<RemoteMessage>

    /**
     * Gets the book content for the given [bookId] and [appVersion].
     */
    @GET("api/statusnew/{appVersion}")
    @Headers(HEADER_ACCEPT_APPLICATION_JSON)
    fun content(@Path(value = "appVersion") appVersion: Int): Call<List<RemoteContentList>>

    /**
     * A version of [content] that allows for possible multiple book IDs. Gets the book content
     * for the given [bookId] and [appVersion]. For now, this method's implementation will just
     * drop the book ID and call the appVersion-only implementation.
     */
    fun content(bookId: Long, appVersion: Int): Call<List<RemoteContentList>>
}
