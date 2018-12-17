/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.*
import io.igist.core.data.local.entity.LocalBook

@Dao
abstract class BookDao {

    /**
     * Inserts a single book (minus the chapter info) into the local database and returns the
     * book ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(book: LocalBook): Long

    /**
     * Updates a book in the local database and returns the number of rows affected.
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(book: LocalBook): Int

    /**
     * Attempts to insert the supplied [book] and, if the book already exists in the database,
     * updates the book instead. Returns the book id.
     */
    @Transaction
    open fun upsert(book: LocalBook): Long {
        val id = insert(book)
        return when (id) {
            -1L -> {
                update(book)
                book.id
            }
            else -> id
        }
    }

    /**
     * Inserts a list of books (minus the chapter info) into the local database and returns
     * a list of the inserted book IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(books: List<LocalBook>): List<Long>

    /**
     * Deletes all books from the local database.
     */
    @Query("DELETE FROM books")
    abstract fun deleteAll()

    /**
     * Retrieves a [LocalBook] from the local database based on the supplied [bookId].
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    abstract fun retrieve(bookId: Long): LocalBook?

    /**
     * Retrieves a list of all [LocalBook]s from the local databse.
     */
    @Query("SELECT * FROM books ORDER BY id > 0 DESC, id")
    abstract fun retrieveAll(): List<LocalBook>

}
