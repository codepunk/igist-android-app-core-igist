package io.igist.core.data.remote.entity

import com.squareup.moshi.Json

/**
 * Remote implementation of a data class representing a chapter of a book.
 */
data class RemoteChapter(

    val title: String?,

    val image: String?,

    val coins: String?,

    val badge: String?,

    @field:Json(name = "badge-name")
    val badgeName: String?,

    @field:Json(name = "badge-desc")
    val badgeDescription: String?,

    val egg: String?,

    @field:Json(name = "egg_frames")
    val eggFrames: String?,

    @field:Json(name = "egg_word")
    val eggWord: String?

)
