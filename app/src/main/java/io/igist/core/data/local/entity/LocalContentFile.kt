/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import io.igist.core.domain.model.FileCategory
import io.igist.core.domain.model.FileType
import java.util.Date

/**
 * Remote implementation of a data class representing a file required by the application.
 */
@Entity(
    tableName = "content_files",
    primaryKeys = ["content_list_id", "category", "filename"],
    foreignKeys = [
        ForeignKey(
            entity = LocalContentList::class,
            parentColumns = ["id"],
            childColumns = ["content_list_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalContentFile(

    /**
     * The ID of the content list that owns this content file.
     */
    @ColumnInfo(name = "content_list_id")
    val contentListId: Long,

    /**
     * The file category of this content file.
     */
    @ColumnInfo(name = "category")
    val category: FileCategory,

    /**
     * The name of the file.
     */
    @ColumnInfo(name = "filename")
    val filename: String,

    /**
     * The modification date of the file.
     */
    @ColumnInfo(name = "date")
    val date: Date

) {

    // region Properties

    /**
     * The [FileType] of this content file.
     */
    @Suppress("UNUSED")
    @Ignore
    val fileType: FileType = FileType.fromFilename(filename) // TODO Very possibly unnecessary

    // endregion Properties

}

