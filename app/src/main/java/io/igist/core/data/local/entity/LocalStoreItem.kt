/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.*
import io.igist.core.domain.model.Currency
import io.igist.core.domain.model.StoreItemType

@Entity(
    tableName = "store_items",
    foreignKeys = [
        ForeignKey(
            entity = LocalStoreCollection::class,
            parentColumns = ["id"],
            childColumns = ["store_collection_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            name = "idx_store_items_store_collection_id_row_order",
            value = ["store_collection_id", "rowOrder"],
            unique = true
        )
    ]
)
data class LocalStoreItem(

    @ColumnInfo(name = "store_collection_id")
    val storeCollectionId: Long,

    @ColumnInfo(name = "content_id")
    val contentId: String,

    @ColumnInfo(name = "content_link")
    val contentLink: String,

    @ColumnInfo(name = "store_icon")
    val storeIcon: String,

    @ColumnInfo(name = "isSoft")
    val currency: Currency,

    val price: Float,

    val title: String,

    val type: StoreItemType,

    @ColumnInfo(name = "rowOrder")
    val order: Int,

    @ColumnInfo(name = "store_desc")
    val description: String

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
