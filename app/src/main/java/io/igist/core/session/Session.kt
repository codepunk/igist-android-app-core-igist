/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.session

import io.igist.core.data.model.User
import io.igist.core.di.component.UserComponent
import io.igist.core.di.scope.UserScope
import java.util.*

private val PENDING_USER = Date(0L).let {
    User(-1, "", "", "", "", false, it, it)
}

/**
 * A class with information about the current user session.
 */
class Session(

    /**
     * The name of the authenticated account.
     */
    val accountName: String,

    /**
     * The type of the authenticated account.
     */
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
    val user: User = PENDING_USER

) {

    // region Constructors

    /**
     * Copy constructor that optionally replaces the supplied [user].
     */
    constructor(session: Session, user: User? = null) : this(
        session.accountName,
        session.accountType,
        session.authToken,
        session.refreshToken,
        session.userComponent,
        when (user) {
            null -> session.user
            else -> user
        }
    )

    // endregion Constructors

}
