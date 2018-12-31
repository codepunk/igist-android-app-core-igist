/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.igist.core.data.local.converters.*
import io.igist.core.data.local.dao.*
import io.igist.core.data.local.entity.*

@Database(
    entities = [
        LocalApi::class,
        LocalBook::class,
        LocalCard::class,
        LocalCardImage::class,
        LocalChapter::class,
        LocalContentList::class,
        LocalContentFile::class,
        LocalStoreCollection::class,
        LocalStoreDepartment::class,
        LocalStoreItem::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    BookModeTypeConverter::class,
    CurrencyConverter::class,
    DateConverter::class,
    FileCategoryTypeConverter::class,
    StoreItemTypeConverter::class
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
     * A [Dao] for the [LocalCard] class.
     */
    abstract fun cardDao(): CardDao

    /**
     * A [Dao] for the [LocalCardImage] class.
     */
    abstract fun cardImageDao(): CardImageDao

    /**
     * A [Dao] for the [LocalChapter] class.
     */
    abstract fun chapterDao(): ChapterDao

    /**
     * A [Dao] for the [LocalContentList] class.
     */
    abstract fun contentListDao(): ContentListDao

    /**
     * A [Dao] for the [LocalContentFile] class.
     */
    abstract fun contentFileDao(): ContentFileDao

    /**
     * A [Dao] for the [LocalStoreCollection] class.
     */
    abstract fun storeCollectionDao(): StoreCollectionDao

    /**
     * A [Dao] for the [LocalStoreDepartment] class.
     */
    abstract fun storeDepartmentDao(): StoreDepartmentDao

    /**
     * A [Dao] for the [LocalStoreItem] class.
     */
    abstract fun storeItemDao(): StoreItemDao

    // endregion Methods

}

