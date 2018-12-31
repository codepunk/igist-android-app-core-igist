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
     * updates the book instead. Returns the book ID.
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
     * Attempts to insert each book in the supplied [books] list and, if the book already exists
     * in the database, updates the book instead. Returns a list of book IDs.
     */
    @Transaction
    open fun upsertAll(books: List<LocalBook>): List<Long> {
        val ids: ArrayList<Long> = ArrayList(insertAll(books))
        val updateList = ArrayList<LocalBook>()

        ids.forEachIndexed { index, result ->
            if (result == -1L) {
                val book = books[index]
                updateList.add(book)
                ids[index] = book.id
            }
        }

        if (updateList.isNotEmpty()) {
            updateAll(updateList)
        }

        return ids
    }

    /**
     * Inserts a list of books (minus the chapter info) into the local database and returns
     * a list of the inserted book IDs.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAll(books: List<LocalBook>): List<Long>

    /**
     * Updates a list of books (minus the chapter info) into the local database and returns
     * the number of rows updated.
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun updateAll(books: List<LocalBook>): Int

    /**
     * Deletes the book with the given [bookId] from the local database.
     */
    @Query("DELETE FROM books WHERE id = :bookId")
    abstract fun delete(bookId: Long)

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
