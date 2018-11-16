/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.session

import android.accounts.AccountManager
import android.content.SharedPreferences
import javax.inject.Singleton

/**
 * Class that manages any currently-logged in user session.
 */
@Singleton
class SessionManager(

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
     * A [Session] instance for storing the current session.
     */
    var session: Session? = null
        private set

    // endregion Properties

}
