/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.session

import io.igist.core.data.model.User
import io.igist.core.di.component.UserComponent
import io.igist.core.di.scope.UserScope
import java.util.*

/**
 * A "dummy" pending user that is set for the time during which we have an auth token but no
 * authenticated user.
 */
private val PENDING_USER = Date(0L).let {
    User(-1, "", "", "", "", false, it, it)
}

/**
 * A class with information about the current user userSession.
 */
class UserSession(

    /**
     * The name of the authenticated account.
     */
    @Suppress("WEAKER_ACCESS")
    val accountName: String,

    /**
     * The type of the authenticated account.
     */
    @Suppress("WEAKER_ACCESS")
    val accountType: String,

    /**
     * The auth token for the authenticated account.
     */
    val authToken: String,

    /**
     * The refresh token for the authenticated account.
     */
    @Suppress("WEAKER_ACCESS")
    val refreshToken: String,

    /**
     * The [UserComponent] for [UserScope]-based dependency injection.
     */
    @Suppress("WEAKER_ACCESS")
    val userComponent: UserComponent,

    /**
     * The authenticated user.
     */
    @Suppress("WEAKER_ACCESS")
    val user: User = PENDING_USER

) {

    // region Constructors

    /**
     * Copy constructor that optionally replaces the supplied [user].
     */
    @Suppress("UNUSED")
    constructor(userSession: UserSession, user: User? = null) : this(
        userSession.accountName,
        userSession.accountType,
        userSession.authToken,
        userSession.refreshToken,
        userSession.userComponent,
        when (user) {
            null -> userSession.user
            else -> user
        }
    )

    // endregion Constructors

}
