/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.*

@Entity(
    tableName = "content_lists",
    foreignKeys = [
        ForeignKey(
            entity = LocalBook::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "idx_content_lists_book_id_content_list_index",
            value = ["book_id", "content_list_index"],
            unique = true
        )
    ]
)
data class LocalContentList(

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "app_version")
    val appVersion: Int,

    @ColumnInfo(name = "content_list_index")
    val contentListIndex: Int,

    val live: Boolean,

    @ColumnInfo(name = "newest_version")
    val newestAppVersion: Int

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
