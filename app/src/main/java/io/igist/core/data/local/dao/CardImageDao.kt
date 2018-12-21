/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalCardImage

@Dao
interface CardImageDao {

    @Query("SELECT * FROM card_images WHERE local_card_id = :cardId ORDER BY image_index")
    fun retrieve(cardId: Long): List<LocalCardImage>

    @Query("SELECT * FROM card_images WHERE local_card_id IN (:cardIds) ORDER BY local_card_id, image_index")
    fun retrieve(cardIds: List<Long>): List<LocalCardImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: LocalCardImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cards: List<LocalCardImage>)

    @Query("DELETE FROM card_images WHERE local_card_id = :cardId")
    fun removeAll(cardId: Long)

}
