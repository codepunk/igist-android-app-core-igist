/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.data.model.Api
import io.igist.core.data.remote.HEADER_ACCEPT_APPLICATION_JSON
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
    @Headers(
        HEADER_ACCEPT_APPLICATION_JSON
    )
    fun api(@Path(value = "apiVersion") apiVersion: Int): Call<Api>

    /**
     * Gets the app API-level information using default values.
     */
    fun api(): Call<Api>
}
