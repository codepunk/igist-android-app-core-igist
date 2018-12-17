/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Locally-cached implementation of a data class representing a book.
 */
@Entity(tableName = "books")
data class LocalBook(

    /**
     * The book ID.
     */
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    /**
     * The book title.
     */
    @ColumnInfo(name = "title")
    val title: String,

    /**
     * The book description.
     */
    @ColumnInfo(name = "description")
    val description: String,

    /**
     * The name of a preview image associated with this book.
     */
    @ColumnInfo(name = "preview_image_name")
    val previewImageName: String,

    /**
     * The book API version. This specifies the "mode" (i.e. beta key required), survey link
     * presented at the end of the book, etc.
     */
    @ColumnInfo(name = "api_version")
    val apiVersion: Int,

    /**
     * The book app version. This specifies the version of the actual content (i.e. images,
     * store data, etc.).
     */
    @ColumnInfo(name = "app_version")
    val appVersion: Int,

    /**
     * Whether this book is currently locked.
     */
    @ColumnInfo(name = "locked")
    val locked: Boolean

)
