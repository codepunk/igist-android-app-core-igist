/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.model

import com.squareup.moshi.Json

/**
 * A data class representing basic API information.
 */
data class Api(

    /**
     * A value that provides certain behavior for the app.
     */
    val igist: Igist,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @field:Json(name = "survey_link")
    val surveyLink: String,

    /**
     * The API version.
     */
    val version: Int

) {

    enum class Igist(value: Int) {

        @field:Json(name = "0")
        NONE(0),

        @field:Json(name = "1")
        SHOW_BETA_PAGE(1);

    }

}
