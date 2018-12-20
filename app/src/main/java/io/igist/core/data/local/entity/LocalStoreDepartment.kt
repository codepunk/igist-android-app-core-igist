/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.*

@Entity(
    tableName = "store_departments",
    foreignKeys = [
        ForeignKey(
            entity = LocalContentList::class,
            parentColumns = ["id"],
            childColumns = ["content_list_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "idx_store_departments_content_list_id_item_index",
            value = ["content_list_id", "item_index"],
            unique = true
        )
    ]
)
data class LocalStoreDepartment(

    @ColumnInfo(name = "content_list_id")
    val contentListId: Long,

    @ColumnInfo(name = "item_index")
    val index: Int,

    val name: String

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
