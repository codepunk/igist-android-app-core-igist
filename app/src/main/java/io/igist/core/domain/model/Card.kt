/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

data class Card(

    val name: String,

    val bio: String,

    val images: List<String>,

    val video: String

)
