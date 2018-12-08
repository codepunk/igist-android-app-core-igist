/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.session

import io.igist.core.domain.model.Api
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSessionManager @Inject constructor() {

    // region Properties

    /**
     * The [Api] instance containing application defaults.
     */
    var api: Api? = null

    /**
     * A [AppSession] instance for storing the current book session.
     */
    var appSession: AppSession? = null
        private set

    // endregion Properties
}
