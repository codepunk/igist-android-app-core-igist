/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.dao

import androidx.room.*
import io.igist.core.data.local.entity.LocalContentFile
import io.igist.core.data.local.entity.LocalContentList

@Dao
interface ContentDao {

    /**
     * Retrieves a [LocalContentList] from the local database based on the supplied [bookId],
     * [appVersion] and [num].
     */
    @Query("SELECT * FROM content_lists WHERE book_id = :bookId AND app_version = :appVersion AND num = :num")
    fun retrieveContentList(bookId: Long, appVersion: Int, num: Int = 1): LocalContentList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContentList(contentList: LocalContentList): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContentLists(contentLists: List<LocalContentList>): LongArray

    /*
    @Query("SELECT * FROM content_files")
    fun retrieveChapterImages(contentListId: Long): List<LocalContentFile>
    */

    @Query("SELECT * FROM content_files WHERE content_list_id = :contentListId AND category = :category")
    fun retrieveContentFiles(contentListId: Long, category: Int): List<LocalContentFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContentFile(contentFile: LocalContentFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContentFiles(contentFiles: List<LocalContentFile>)

    @Query("DELETE FROM content_files WHERE content_list_id = :contentListId AND category = :category")
    fun removeContentFiles(contentListId: Long, category: Int)

}
