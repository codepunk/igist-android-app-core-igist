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
fun LocalApi.toApi(): Api = Api(bookId, apiVersion, bookMode, surveyLink)

/**
 * Converts a nullable [LocalApi] to a nullable domain [Api].
 */
fun LocalApi?.toApiOrNull(): Api? = this?.let {
    Api(it.bookId, it.apiVersion, it.bookMode, it.surveyLink)
}

/**
 * Converts a [RemoteApi] to a [LocalApi].
 */
fun RemoteApi.toLocalApi(bookId: Long): LocalApi =
    LocalApi(bookId, apiVersion, bookMode, surveyLink)

/**
 * Converts a nullable [RemoteApi] to a nullable [LocalApi].
 */
@Suppress("UNUSED")
fun RemoteApi?.toLocalApiOrNull(bookId: Long): LocalApi? = this?.let {
    LocalApi(bookId, it.apiVersion, it.bookMode, it.surveyLink)
}
