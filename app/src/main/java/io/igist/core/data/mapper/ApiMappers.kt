/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.LocalApi
import io.igist.core.data.remote.entity.RemoteApi
import io.igist.core.domain.model.Api

/**
 * Converts a [LocalApi] to a domain [Api].
 */
fun LocalApi.toApi(): Api = Api(version, igistMode, surveyLink)

/**
 * Converts a nullable [LocalApi] to a nullable domain [Api].
 */
fun LocalApi?.toApiOrNull(): Api? = this?.let {
    Api(it.version, it.igistMode, it.surveyLink)
}

/**
 * Converts a [RemoteApi] to a [LocalApi].
 */
fun RemoteApi.toLocalApi(): LocalApi = LocalApi(version, igistMode, surveyLink)

/**
 * Converts a nullable [RemoteApi] to a nullable [LocalApi].
 */
@Suppress("UNUSED")
fun RemoteApi?.toLocalApiOrNull(): LocalApi? = this?.let {
    LocalApi(it.version, it.igistMode, it.surveyLink)
}
