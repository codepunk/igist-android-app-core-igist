package io.igist.core.domain.model

/**
 * A data class representing a chapter of a book.
 */
data class Chapter(

    val bookId: Long,

    val number: Int,

    val title: String?,

    val image: String?,

    val coins: Int,

    val badge: String?,

    val badgeName: String?,

    val badgeDescription: String?,

    val egg: String?,

    val eggFrames: Float,

    val eggWord: String?

)
