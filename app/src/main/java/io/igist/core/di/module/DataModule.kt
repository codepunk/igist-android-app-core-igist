/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.igist.core.data.local.database.IgistDb
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.remote.adapter.BooleanIntAdapter
import io.igist.core.data.remote.adapter.DateJsonAdapter
import io.igist.core.data.remote.converter.MoshiEnumConverterFactory
import io.igist.core.data.remote.interceptor.AuthorizationInterceptor
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.data.remote.webservice.AppWebserviceWrapper
import io.igist.core.data.repository.AppRepositoryImpl2
import io.igist.core.data.repository.BookRepositoryImpl
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.session.AppSessionManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

// region Constants

/**
 * The default cache size for OkHttp client cache.
 */
private const val CACHE_SIZE: Long = 10 * 1024 * 1024

// endregion Constants

/**
 * A dependency injection module that provides all data access-related dependencies for both
 * remote and local data retrieval and storage.
 */
@Suppress("unused")
@Module(includes = [AppModule::class])
class DataModule {

    // region Methods

    /**
     * Provides an instance of [Cache] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesCache(@ApplicationContext context: Context): Cache =
        Cache(context.cacheDir, CACHE_SIZE)

    /**
     * Provides an instance of [OkHttpClient] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesOkHttpClient(
        cache: Cache,
        authorizationInterceptor: AuthorizationInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(authorizationInterceptor)
        .build()

    /**
     * Provides an instance of [Moshi] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesMoshi(
        booleanIntAdapter: BooleanIntAdapter,
        dateJsonAdapter: DateJsonAdapter
    ): Moshi = Moshi.Builder()
        .add(booleanIntAdapter)
        .add(Date::class.java, dateJsonAdapter)
        .build()

    /**
     * Provides an instance of [MoshiConverterFactory] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi)

    /**
     * Provides an instance of [Retrofit] for dependency injection.
     */
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory,
        moshiEnumConverterFactory: MoshiEnumConverterFactory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://igist.io")
        .addConverterFactory(moshiConverterFactory)
        .addConverterFactory(moshiEnumConverterFactory)
        .build()

    /**
     * Provides an instance of [AppWebservice] for making authorization API calls.
     */
    @Provides
    @Singleton
    fun providesAppWebservice(
        retrofit: Retrofit
    ): AppWebservice = AppWebserviceWrapper(retrofit.create(AppWebservice::class.java))

    /**
     * Provides an [IgistDb] instance.
     */
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): IgistDb = Room
        .databaseBuilder(context, IgistDb::class.java, "igist.db")
        .fallbackToDestructiveMigration()
        .build()

    /**
     * Creates an [ApiDao] instance.
     */
    @Singleton
    @Provides
    fun providesApiDao(db: IgistDb): ApiDao = db.apiDao()

    /**
     * Creates a [BookDao] instance.
     */
    @Singleton
    @Provides
    fun providesBookDao(db: IgistDb): BookDao = db.bookDao()

    /**
     * Creates an [AppRepository] instance.
     */
    @Singleton
    @Provides
    fun providesAppRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        appDao: ApiDao,
        appWebservice: AppWebservice,
        appSessionManager: AppSessionManager
    ): AppRepository = AppRepositoryImpl2(
        context,
        sharedPreferences,
        appDao,
        appWebservice,
        appSessionManager
    )

    /**
     * Creates a [BookRepository] instance.
     */
    @Singleton
    @Provides
    fun providesBookRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        bookDao: BookDao,
        moshi: Moshi
    ): BookRepository = BookRepositoryImpl(context, sharedPreferences, bookDao, moshi)

    // endregion Methods

}
