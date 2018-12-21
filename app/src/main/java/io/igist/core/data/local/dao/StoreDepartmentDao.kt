/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.*
import io.igist.core.data.local.entity.LocalStoreDepartment

@Dao
interface StoreDepartmentDao {

    @Query("SELECT * FROM store_departments WHERE content_list_id = :contentListId")
    fun retrieve(contentListId: Long): List<LocalStoreDepartment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeDepartment: LocalStoreDepartment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeDepartments: List<LocalStoreDepartment>): LongArray

    @Query("DELETE FROM store_departments WHERE content_list_id = :contentListId")
    fun removeAll(contentListId: Long)

}
