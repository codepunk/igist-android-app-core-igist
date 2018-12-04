/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import io.igist.core.data.local.IgistDb
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.di.qualifier.ApplicationContext
import javax.inject.Singleton

@Suppress("unused")
@Module(includes = [AppModule::class])
class DatabaseModule {

    // region Methods

    /**
     * Provides an [IgistDb] instance.
     */
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): IgistDb {
        return Room
            .databaseBuilder(context, IgistDb::class.java, "igist.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Creates an [ApiDao] instance.
     */
    @Singleton
    @Provides
    fun providesApiDao(db: IgistDb): ApiDao {
        return db.apiDao()
    }

    // endregion Methods

}
