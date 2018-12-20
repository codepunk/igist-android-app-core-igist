/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.*

@Entity(
    tableName = "store_collections",
    foreignKeys = [
        ForeignKey(
            entity = LocalStoreDepartment::class,
            parentColumns = ["id"],
            childColumns = ["store_department_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "idx_store_collections_store_department_id_item_index",
            value = ["content_list_id", "item_index"],
            unique = true
        )
    ]
)
data class LocalStoreCollection(

    @ColumnInfo(name = "content_list_id")
    val contentListId: Long,

    @ColumnInfo(name = "store_department_id")
    val storeDepartmentId: Long,

    @ColumnInfo(name = "item_index")
    val index: Int,

    val name: String

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
