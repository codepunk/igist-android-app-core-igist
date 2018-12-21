/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalCard

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE content_list_id = :contentListId")
    fun retrieve(contentListId: Long): List<LocalCard>

    @Query("SELECT * FROM cards WHERE content_list_id IN (:contentListIds) ORDER BY content_list_id, card_index")
    fun retrieve(contentListIds: List<Long>): List<LocalCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(card: LocalCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cards: List<LocalCard>): LongArray

    @Query("DELETE FROM cards WHERE content_list_id = :contentListId")
    fun removeAll(contentListId: Long)

}
