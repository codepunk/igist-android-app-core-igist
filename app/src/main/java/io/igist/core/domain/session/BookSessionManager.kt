/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSessionManager @Inject constructor() {

    // region Properties

    /**
     * A [BookSession] instance for storing the current book session.
     */
    var bookSession: BookSession? = null
        private set

    // endregion Properties
}
