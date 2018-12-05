/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.ApiLocal
import io.igist.core.domain.model.Api

/**
 * [Dao] class for performing API-related operations on the local database.
 */
@Dao
abstract class ApiDao {

    /**
     * Inserts a single book (minus the chapter info) into the local database and returns the
     * book id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(api: ApiLocal): Long

    /**
     * Retrieves [Api] information the local database based on the supplied [version].
     */
    @Query("SELECT * FROM api WHERE version = :version")
    abstract fun retrieve(version: Int): ApiLocal?

}
