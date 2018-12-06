/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.igist.core.domain.model.IgistMode

/**
 * A data class representing basic API information to be stored in the local database.
 */
@Entity(tableName = "api")
data class LocalApi(

    /**
     * The API version.
     */
    @PrimaryKey
    @ColumnInfo(name = "version")
    val version: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    @ColumnInfo(name = "igist")
    val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @ColumnInfo(name = "survey_link")
    val surveyLink: String

)
