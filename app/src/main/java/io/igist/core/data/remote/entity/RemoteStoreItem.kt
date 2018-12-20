/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json
import io.igist.core.domain.model.Currency
import io.igist.core.domain.model.StoreItemType

class RemoteStoreItem(

    @field:Json(name = "content_id")
    val contentId: String,

    @field:Json(name = "content_link")
    val contentLink: String,

    @field:Json(name = "store_icon")
    val storeIcon: String,

    @field:Json(name = "isSoft")
    val currency: Currency,

    val price: Float,

    val title: String,

    val type: StoreItemType,

    @field:Json(name = "rowOrder")
    val order: Int,

    @field:Json(name = "store_desc")
    val description: String

)
