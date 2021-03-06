/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.igist.core.data.local.entity.LocalContentList

@Dao
interface ContentListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contentList: LocalContentList): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contentLists: List<LocalContentList>): LongArray

    @Query("DELETE FROM content_lists WHERE book_id = :bookId AND content_list_index = :index")
    fun remove(bookId: Long, index: Int)

    /**
     * Retrieves a [LocalContentList] from the local database based on the supplied [bookId],
     * [appVersion] and [index].
     */
    @Query("SELECT * FROM content_lists WHERE book_id = :bookId AND app_version = :appVersion AND content_list_index = :index")
    fun retrieve(bookId: Long, appVersion: Int, index: Int = 0): LocalContentList?

}
