/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

/**
 * A data class representing all content required by a book.
 */
data class ContentList(

    val bookId: Long,

    val appVersion: Int,

    val live: Boolean,

    val newestAppVersion: Int,

    val chapterImages: List<ContentFile>?,

    val sputniks: List<ContentFile>?,

    val badges: List<ContentFile>?,

    val storefront: List<ContentFile>?,

    val storeData: Map<String, List<Map<String, List<StoreItem>>>>?,

    val cardData: Map<String, Card>?

) {

    /**
     * Returns the [ContentFile] list associated with the supplied [fileCategory].
     */
    fun getContentFiles(fileCategory: FileCategory): List<ContentFile>? = when (fileCategory) {
        FileCategory.CHAPTER_IMAGE -> chapterImages
        FileCategory.SPUTNIK -> sputniks
        FileCategory.BADGE -> badges
        FileCategory.STOREFRONT -> storefront
        else -> null
    }
}
