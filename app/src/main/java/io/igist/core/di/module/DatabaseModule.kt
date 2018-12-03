/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import io.igist.core.data.local.IgistDb
import io.igist.core.data.local.dao.ApiDao
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
    fun provideDb(app: Application): IgistDb {
        return Room
            .databaseBuilder(app, IgistDb::class.java, "igist.db")
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
