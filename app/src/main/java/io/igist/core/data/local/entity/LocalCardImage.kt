/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "card_images",
    primaryKeys = ["local_card_id", "image_index"],
    foreignKeys = [
        ForeignKey(
            entity = LocalCard::class,
            parentColumns = ["id"],
            childColumns = ["local_card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalCardImage(

    @ColumnInfo(name = "local_card_id")
    val localCardId: Long,

    @ColumnInfo(name = "image_index")
    val imageIndex: Int,

    @ColumnInfo(name = "image_name")
    val imageName: String

)
