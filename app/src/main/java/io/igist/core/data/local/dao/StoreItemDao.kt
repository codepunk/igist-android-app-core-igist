/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalStoreItem

@Dao
interface StoreItemDao {

    @Query("SELECT * FROM store_items WHERE collection_id = :collectionId ORDER BY rowOrder")
    fun retrieve(collectionId: Long): List<LocalStoreItem>

    @Query("SELECT * FROM store_items WHERE collection_id IN (:collectionIds) ORDER BY collection_id, rowOrder")
    fun retrieve(collectionIds: List<Long>): List<LocalStoreItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeItem: LocalStoreItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(storeItems: List<LocalStoreItem>): LongArray

    @Query("DELETE FROM store_items WHERE collection_id = :collectionId")
    fun removeAll(collectionId: Long)

}
