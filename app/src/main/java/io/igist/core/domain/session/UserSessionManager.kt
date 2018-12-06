/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import android.accounts.AccountManager
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user userSession.
 */
@Singleton
class UserSessionManager @Inject constructor(

    /**
     * The account manager.
     */
    @Suppress("UNUSED")
    private val accountManager: AccountManager,

    /**
     * The application [SharedPreferences].
     */
    @Suppress("UNUSED")
    private val sharedPreferences: SharedPreferences

) {

    // region Properties

    /**
     * A [UserSession] instance for storing the current user session.
     */
    var userSession: UserSession? = null
        private set

    // endregion Properties

}
