/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

/**
 * A data class representing basic API information.
 */
data class Api(

    /**
     * The API version.
     */
    val version: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    val surveyLink: String

)
