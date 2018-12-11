/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.*
import io.igist.core.data.local.entity.LocalBook

@Dao
interface BookDao {

    /**
     * Inserts a single book (minus the chapter info) into the local database and returns the
     * book ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: LocalBook): Long

    /**
     * Inserts a list of books (minus the chapter info) into the local database and returns
     * a list of the inserted book IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(books: List<LocalBook>): List<Long>

    /**
     * Deletes all books from the local database.
     */
    @Query("DELETE FROM books")
    fun deleteAll()

    /**
     * Retrieves a [LocalBook] from the local database based on the supplied [bookId].
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun retrieve(bookId: Long): LocalBook?

    /**
     * Retrieves a list of all [LocalBook]s from the local databse.
     */
    @Query("SELECT * FROM books ORDER BY id > 0 DESC, id")
    fun retrieveAll(): List<LocalBook>

}
