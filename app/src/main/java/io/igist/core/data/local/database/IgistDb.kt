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
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.local.entity.LocalApi
import io.igist.core.data.local.entity.LocalBook

@Database(
    entities = [
        LocalBook::class,
        LocalApi::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    BookModeTypeConverter::class
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

    // endregion Methods

}

