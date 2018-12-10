/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

/**
 * A data class representing all content required by a book.
 */
// TODO How do I get book ID in here?
data class ContentList(

    val version: Int,

    val live: Boolean,

    val newestVersion: Int,

    val chapterImages: List<ContentFile>?,

    val sputniks: List<ContentFile>?,

    val badges: List<ContentFile>?,

    val storefront: List<ContentFile>?

)