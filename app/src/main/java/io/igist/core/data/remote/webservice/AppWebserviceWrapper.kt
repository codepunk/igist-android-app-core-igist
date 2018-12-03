/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.BuildConfig
import io.igist.core.data.model.Api
import retrofit2.Call

/**
 * Implementation of [AppWebservice] that allows for default arguments by wrapping another
 * instance ([base]) and passing default arguments to its methods where appropriate.
 */
class AppWebserviceWrapper(private val base: AppWebservice) : AppWebservice {

    /**
     * Gets the app API-level information.
     */
    override fun api(apiVersion: Int): Call<Api> = base.api(apiVersion)

    /**
     * Gets the app API-level information using default values.
     */
    override fun api(): Call<Api> = base.api(BuildConfig.API_VERSION)

}
