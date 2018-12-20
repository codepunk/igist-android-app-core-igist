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
    primaryKeys = ["local_card_id", "order"],
    foreignKeys = [
        ForeignKey(
            entity = LocalCard::class,
            parentColumns = ["id"],
            childColumns = ["local_card_id"]
        )
    ]
)
data class LocalCardImage(

    @ColumnInfo(name = "local_card_id")
    val localCardId: Long,

    val order: Int,

    @ColumnInfo(name = "image_name")
    val imageName: String

)
