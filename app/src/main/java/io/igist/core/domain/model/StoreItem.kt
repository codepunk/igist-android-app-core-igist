/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

data class StoreItem(

    val contentId: String,

    val contentLink: String,

    val storeIcon: String,

    val currency: Currency,

    val price: Float,

    val title: String,

    val type: StoreItemType,

    val order: Int,

    val description: String

)
