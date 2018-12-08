/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig.API_VERSION
import io.igist.core.BuildConfig.APP_VERSION

interface AppRepository {

    fun load(
        apiVersion: Int = API_VERSION,
        appVersion: Int = APP_VERSION,
        alwaysFetchApi: Boolean = true,
        alwaysValidateBetaKey: Boolean = true
    ): LiveData<DataUpdate<Int, Boolean>>

}
