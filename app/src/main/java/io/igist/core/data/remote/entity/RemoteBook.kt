/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json

/**
 * Remote implementation of a data class representing a book.
 */
data class RemoteBook(

    /**
     * The book ID.
     */
    val id: Long,

    /**
     * The book title.
     */
    val title: String,

    /**
     * The book description.
     */
    val description: String,

    /**
     * The name of a preview image associated with this book.
     */
    @field:Json(name = "preview_image_name")
    val previewImageName: String,

    /**
     * The book API version. This specifies the "mode" (i.e. beta key required), survey link
     * presented at the end of the book, etc.
     */
    @field:Json(name = "api_version")
    val apiVersion: Int,

    /**
     * The book app version. This specifies the version of the actual content (i.e. images,
     * store data, etc.).
     */
    @field:Json(name = "app_version")
    val appVersion: Int,

    /**
     * Whether this book is currently locked.
     */
    val locked: Boolean,

    /**
     * The name of the remote PLIST file containing the book's chapter data.
     */
    @field:Json(name = "plist_file")
    val plistFile: String

)
