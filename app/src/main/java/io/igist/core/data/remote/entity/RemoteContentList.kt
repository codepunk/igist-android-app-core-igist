/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json

/**
 * Remote implementation of a data class representing all content required by a book.
 */
data class RemoteContentList(

    val version: Int,

    val live: Boolean,

    @field:Json(name = "newest_version")
    val newestVersion: Int,

    @field:Json(name = "chapter_images")
    val chapterImages: List<RemoteContentFile>?,

    val sputniks: List<RemoteContentFile>?,

    val badges: List<RemoteContentFile>?,

    @field:Json(name = "store_front")
    val storefront: List<RemoteContentFile>?

)
