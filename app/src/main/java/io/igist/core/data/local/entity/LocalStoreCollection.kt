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
            childColumns = ["department_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "idx_store_collections_department_id_collection_index",
            value = ["department_id", "category_index", "collection_index"],
            unique = true
        )
    ]
)
data class LocalStoreCollection(

    @ColumnInfo(name = "department_id")
    val departmentId: Long,

    @ColumnInfo(name = "category_index")
    val categoryIndex: Int,

    @ColumnInfo(name = "collection_index")
    val collectionIndex: Int,

    val name: String

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
