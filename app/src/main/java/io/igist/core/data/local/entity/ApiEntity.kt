/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.igist.core.data.contract.IApi
import io.igist.core.data.model.IgistMode

/**
 * A data class representing basic API information to be stored in the local database.
 */
@Entity(tableName = "api")
data class ApiEntity(

    /**
     * The API version.
     */
    @PrimaryKey
    @ColumnInfo(name = "version")
    override val version: Int,

    /**
     * The "mode" (i.e. behavior) associated with the current API version.
     */
    @ColumnInfo(name = "igist")
    override val igistMode: IgistMode,

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    @ColumnInfo(name = "survey_link")
    override val surveyLink: String

) : IApi {

    // region constructors

    /**
     * A copy constructor for mapping one implementation of [IApi] to another.
     */
    constructor(api: IApi) : this(
        api.version,
        api.igistMode,
        api.surveyLink
    )

    // endregion constructors

}
