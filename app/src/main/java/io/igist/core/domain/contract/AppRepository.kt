/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.BuildConfig
import io.igist.core.domain.model.Api

interface AppRepository {

    fun getApi(
        apiVersion: Int = BuildConfig.API_VERSION,
        alwaysFetch: Boolean = true
    ): LiveData<DataUpdate<Api, Api>>

}
