/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.model

import com.squareup.moshi.Json
import io.igist.core.data.contract.IApi

/**
 * A data class representing basic API information.
 */
data class Api(

    /**
     * The API version.
     */
    @field:Json(name = "version")
    override val version: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    @field:Json(name = "igist")
    override val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @field:Json(name = "survey_link")
    override val surveyLink: String

) : IApi {

    // region constructors

    /**
     * A copy constructor for mapping one implementation of [IApi] to another.
     */
    constructor(api: IApi) : this(
        api.version,
        api.igistMode,
        api.surveyLink
    )

    // endregion constructors

}
