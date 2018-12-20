/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json
import io.igist.core.domain.model.RemoteCard

/**
 * Remote implementation of a data class representing all content required by a book.
 */
data class RemoteContentList(

    @field:Json(name = "version")
    val appVersion: Int,

    val live: Boolean,

    @field:Json(name = "newest_version")
    val newestAppVersion: Int,

    @field:Json(name = "chapter_images")
    val chapterImages: List<RemoteContentFile>?,

    val sputniks: List<RemoteContentFile>?,

    val badges: List<RemoteContentFile>?,

    @field:Json(name = "store_front")
    val storefront: List<RemoteContentFile>?,

    @field:Json(name = "store_data")
    val storeData: Map<String, List<Map<String, List<RemoteStoreItem>>>>?,

    @field:Json(name = "card_data")
    val cardData: Map<String, RemoteCard>?

)
