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
            name = "idx_content_lists_book_id_num_index",
            value = ["book_id", "num"],
            unique = true
        )
    ]
)
class LocalContentList(

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "app_version")
    val appVersion: Int,

    @ColumnInfo(name = "num")
    val num: Int,

    @ColumnInfo(name = "live")
    val live: Boolean,

    @ColumnInfo(name = "newest_version")
    val newestAppVersion: Int

    /*
    val chapterImages: List<ContentFile>?,

    val sputniks: List<ContentFile>?,

    val badges: List<ContentFile>?,

    val storefront: List<ContentFile>?
    */

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
