/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

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
    val familyName: String,

    /**
     * The user's given name.
     */
    val givenName: String,

    /**
     * Whether the user is active.
     */
    val active: Boolean,

    /**
     * The date the user was created.
     */
    val createdAt: Date,

    /**
     * The date the user was last updated.
     */
    val updatedAt: Date

)
