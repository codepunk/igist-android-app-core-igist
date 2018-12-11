/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalApi

/**
 * [Dao] class for performing API-related operations on the local database.
 */
@Dao
interface ApiDao {

    /**
     * Inserts a single book (minus the chapter info) into the local database and returns the
     * book id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(api: LocalApi): Long

    /**
     * Retrieves [LocalApi] information from the local database based on the supplied [bookId]
     * and [apiVersion].
     */
    @Query("SELECT * FROM apis WHERE book_id = :bookId AND api_version = :apiVersion")
    fun retrieve(bookId: Long, apiVersion: Int): LocalApi?

}
