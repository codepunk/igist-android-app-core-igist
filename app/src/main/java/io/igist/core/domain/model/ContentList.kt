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

    val appVersion: Int,

    val live: Boolean,

    val newestAppVersion: Int,

    val chapterImages: List<ContentFile>?,

    val sputniks: List<ContentFile>?,

    val badges: List<ContentFile>?,

    val storefront: List<ContentFile>?,

    val storeData: Map<String, List<Map<String, List<StoreItem>>>>?,

    val cardData: Map<String, List<Card>>?

)
