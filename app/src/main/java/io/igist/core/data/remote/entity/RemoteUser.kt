/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json
import io.igist.core.data.remote.annotation.BooleanInt
import java.util.*

/**
 * A data class representing a user.
 */
data class User(

    /**
     * The user id.
     */
    val id: Long,

    /**
     * The username.
     */
    val username: String,

    /**
     * The user's email.
     */
    val email: String,

    /**
     * The user's family name.
     */
    @field:Json(name = "family_name")
    val familyName: String,

    /**
     * The user's given name.
     */
    @field:Json(name = "given_name")
    val givenName: String,

    /**
     * Whether the user is active.
     */
    @field:Json(name = "active")
    @field:BooleanInt
    val active: Boolean,

    /**
     * The date the user was created.
     */
    @field:Json(name = "created_at")
    val createdAt: Date,

    /**
     * The date the user was last updated.
     */
    @field:Json(name = "updated_at")
    val updatedAt: Date

)
