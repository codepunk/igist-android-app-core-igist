/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json
import io.igist.core.domain.model.IgistMode

/**
 * Remote implementation of a data class representing basic API information.
 */
data class RemoteApi(

    /**
     * The API version.
     */
    @field:Json(name = "version")
    val apiVersion: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    @field:Json(name = "igist")
    val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @field:Json(name = "survey_link")
    val surveyLink: String

)
