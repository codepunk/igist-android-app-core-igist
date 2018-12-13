/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import io.igist.core.domain.model.BookMode

/**
 * Locally-cached implementation of a data class representing basic API information.
 */
@Entity(
    tableName = "apis",
    primaryKeys = ["book_id", "api_version"]
)
data class LocalApi(

    /**
     * The book ID.
     */
    @ColumnInfo(name = "book_id")
    val bookId: Long,

    /**
     * The API version.
     */
    @ColumnInfo(name = "api_version")
    val apiVersion: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    @ColumnInfo(name = "igist")
    val bookMode: BookMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @ColumnInfo(name = "survey_link")
    val surveyLink: String

)
