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
     * The book ID.
     */
    val bookId: Long,

    /**
     * The API version.
     */
    val apiVersion: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    val surveyLink: String

)
