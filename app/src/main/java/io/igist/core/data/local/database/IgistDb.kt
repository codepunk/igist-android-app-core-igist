/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.igist.core.data.local.converters.BookModeTypeConverter
import io.igist.core.data.local.converters.DateConverter
import io.igist.core.data.local.converters.FileCategoryTypeConverter
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.local.dao.ContentDao
import io.igist.core.data.local.entity.LocalApi
import io.igist.core.data.local.entity.LocalBook
import io.igist.core.data.local.entity.LocalContentFile
import io.igist.core.data.local.entity.LocalContentList

@Database(
    entities = [
        LocalBook::class,
        LocalApi::class,
        LocalContentList::class,
        LocalContentFile::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    BookModeTypeConverter::class,
    DateConverter::class,
    FileCategoryTypeConverter::class
)
abstract class IgistDb : RoomDatabase() {

    // region Methods

    /**
     * A [Dao] for the [LocalApi] class.
     */
    abstract fun apiDao(): ApiDao

    /**
     * A [Dao] for the [LocalBook] class.
     */
    abstract fun bookDao(): BookDao

    /**
     * A [Dao] for the [LocalContentList] and related classes.
     */
    abstract fun contentDao(): ContentDao

    // endregion Methods

}

