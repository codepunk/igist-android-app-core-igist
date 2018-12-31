package io.igist.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Locally-cached implementation of a data class representing a chapter of a book.
 */
@Entity(
    tableName = "chapters",
    primaryKeys = ["book_id", "number"],
    foreignKeys = [
        ForeignKey(
            entity = LocalBook::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalChapter(

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    val number: Int,

    val title: String?,

    val image: String?,

    val coins: Int,

    val badge: String?,

    @ColumnInfo(name = "badge_name")
    val badgeName: String?,

    @ColumnInfo(name = "badge_description")
    val badgeDescription: String?,

    val egg: String?,

    @ColumnInfo(name = "egg_frames")
    val eggFrames: Float,

    @ColumnInfo(name = "egg_word")
    val eggWord: String?

)
