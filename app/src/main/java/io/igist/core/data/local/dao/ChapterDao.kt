package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalChapter

@Dao
interface ChapterDao {

    /**
     * Inserts a single book (minus the chapter info) into the local database and returns the
     * book id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chapter: LocalChapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chapters: List<LocalChapter>)

    /**
     * Retrieves a [LocalChapter] from the local database based on the supplied [bookId]
     * and [chapterNumber].
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND number = :chapterNumber")
    fun retrieve(bookId: Long, chapterNumber: Int): LocalChapter?

    /**
     * Retrieves all [LocalChapter]s from the local database based on the supplied [bookId].
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId")
    fun retrieveAll(bookId: Long): List<LocalChapter>?

}
