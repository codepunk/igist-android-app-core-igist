/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.ApiLocal
import io.igist.core.data.util.Mapper
import io.igist.core.domain.model.Api
import javax.inject.Inject

class ApiLocalToDomainMapper @Inject constructor() : Mapper<ApiLocal, Api>() {

    override fun map(source: ApiLocal): Api = Api(
        source.version,
        source.igistMode,
        source.surveyLink
    )

}
