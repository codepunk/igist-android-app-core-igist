package io.igist.core.data.mapper

import io.igist.core.data.local.entity.LocalChapter
import io.igist.core.data.remote.entity.RemoteChapter
import io.igist.core.domain.model.Chapter

/**
 * Converts a [LocalBook] to a domain [Book].
 */
fun LocalChapter.toChapter(): Chapter =
    Chapter(
        bookId,
        number,
        title,
        image,
        coins,
        badge,
        badgeName,
        badgeDescription,
        egg,
        eggFrames,
        eggWord
    )

/**
 * Converts a nullable [LocalChapter] to a nullable domain [Chapter].
 */
fun LocalChapter?.toChapterOrNull(): Chapter? = this?.toChapter()

/**
 * Converts a [List] of [LocalChapter]s to a list of domain [Chapter]s.
 */
fun List<LocalChapter>.toChapters(): List<Chapter> = map { it.toChapter() }

/**
 * Converts a nullable [List] of [LocalChapter]s to a nullable list of domain [Chapter]s.
 */
fun List<LocalChapter>?.toChaptersOrNull(): List<Chapter>? = this?.toChapters()

/**
 * Converts a [RemoteChapter] to a [LocalChapter].
 */
fun RemoteChapter.toLocalChapter(bookId: Long, number: Int): LocalChapter =
    LocalChapter(
        bookId,
        number,
        title,
        image,
        when {
            coins.isNullOrEmpty() -> 0
            else -> Integer.parseInt(coins)
        },
        badge,
        badgeName,
        badgeDescription,
        egg,
        eggFrames?.toFloatOrNull() ?: 0.0f,
        eggWord
    )

/**
 * Converts a nullable [RemoteChapter] to a nullable [LocalChapter].
 */
@Suppress("UNUSED")
fun RemoteChapter?.toLocalChapterOrNull(bookId: Long, number: Int): LocalChapter? =
    this?.toLocalChapter(bookId, number)

/**
 * Converts a [List] of [RemoteChapter]s to a list of [LocalChapter]s.
 */
fun List<RemoteChapter>.toLocalChapters(bookId: Long): List<LocalChapter> =
    mapIndexed { index, remoteChapter ->
        remoteChapter.toLocalChapter(bookId, index + 1)
    }


