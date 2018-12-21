/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalStoreCollection

@Dao
interface StoreCollectionDao {

    @Query("SELECT * FROM store_collections WHERE department_id = :departmentId ORDER BY category_index, collection_index")
    fun retrieve(departmentId: Long): List<LocalStoreCollection>

    @Query("SELECT * FROM store_collections WHERE department_id IN (:departmentIds) ORDER BY department_id, category_index, collection_index")
    fun retrieve(departmentIds: List<Long>): List<LocalStoreCollection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeCollection: LocalStoreCollection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeCollections: List<LocalStoreCollection>): LongArray

    @Query("DELETE FROM store_collections WHERE department_id = :departmentId")
    fun removeAll(departmentId: Long)

}
