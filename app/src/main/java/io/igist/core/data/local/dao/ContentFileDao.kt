/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.*
import io.igist.core.data.local.entity.LocalContentFile

@Dao
interface ContentFileDao {

    @Query("SELECT * FROM content_files WHERE content_list_id = :contentListId AND category = :category")
    fun retrieve(contentListId: Long, category: Int): List<LocalContentFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contentFile: LocalContentFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contentFiles: List<LocalContentFile>)

    @Query("DELETE FROM content_files WHERE content_list_id = :contentListId AND category = :category")
    fun removeAll(contentListId: Long, category: Int)

}
