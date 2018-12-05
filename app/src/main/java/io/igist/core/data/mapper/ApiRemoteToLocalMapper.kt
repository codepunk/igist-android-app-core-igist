/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.ApiLocal
import io.igist.core.data.remote.entity.ApiRemote
import io.igist.core.data.util.Mapper
import javax.inject.Inject

class ApiRemoteToLocalMapper @Inject constructor() : Mapper<ApiRemote, ApiLocal>() {

    override fun map(source: ApiRemote): ApiLocal = ApiLocal(
        source.version,
        source.igistMode,
        source.surveyLink
    )

}
