/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import android.util.Log
import io.igist.core.domain.model.Api
import io.igist.core.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A manager that keeps track of all app-related session information, for example, the currently-
 * selected book.
 */
@Singleton
class AppSessionManager @Inject constructor() {

    // region Properties

    /**
     * The currently-selected [Book].
     */
    var book: Book? = null
        set(value) {
            if (field != value) {
                Log.d("AppSessionManager", "book.set: value=$value")
                field = value
            }
        }

    /**
     * The [Api] instance containing application defaults for the currently-selected [Book].
     */
    var api: Api? = null

    /**
     * A [AppSession] instance for storing the current book session.
     */
    @Suppress("UNUSED")
    var appSession: AppSession? = null
        private set

    // endregion Properties
}
