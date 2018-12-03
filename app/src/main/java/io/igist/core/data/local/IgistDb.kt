/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.igist.core.data.local.converters.IgistModeTypeConverter
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.entity.ApiEntity

@Database(
    entities = [
        ApiEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    IgistModeTypeConverter::class
)
abstract class IgistDb : RoomDatabase() {

    // region Methods

    /**
     * a [Dao] for the [ApiEntity] class.
     */
    abstract fun apiDao(): ApiDao

    // endregion Methods

}

