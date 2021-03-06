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
import io.igist.core.BuildConfig
import io.igist.core.data.local.dao.*
import io.igist.core.data.local.database.IgistDb
import io.igist.core.data.remote.adapter.BooleanIntAdapter
import io.igist.core.data.remote.adapter.DateJsonAdapter
import io.igist.core.data.remote.converter.MoshiEnumConverterFactory
import io.igist.core.data.remote.interceptor.AuthorizationInterceptor
import io.igist.core.data.remote.webservice.*
import io.igist.core.data.repository.AppRepositoryImpl
import io.igist.core.data.repository.BookRepositoryImpl
import io.igist.core.data.repository.ChapterRepositoryImpl
import io.igist.core.data.resolver.BookResolver
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.AppRepository
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.contract.ChapterRepository
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
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(moshiConverterFactory)
        .addConverterFactory(moshiEnumConverterFactory)
        .build()

    /**
     * Provides an instance of [AppWebservice] for making app API calls.
     */
    @Provides
    @Singleton
    fun providesAppWebservice(
        retrofit: Retrofit
    ): AppWebservice = AppWebserviceWrapper(retrofit.create(AppWebservice::class.java))

    /**
     * Provides an instance of [BookWebservice] for making book API calls.
     * Note that currently there is no endpoint for handling multiple books so we'll
     * code the logic into a local BookWebserviceImpl implementation for now.
     */
    @Provides
    @Singleton
    fun providesBookWebservice(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): BookWebservice = BookWebserviceImpl(context, moshi)

    /**
     * Provides an instance of [ChapterWebservice] for making chapter API calls.
     * Note that chapter data is stored on the network as a PLIST file so we'll
     * code the logic into a ChapterServiceImpl class to mimic a traditional Retrofit
     * webservice call.
     */
    @Provides
    @Singleton
    fun providesChapterWebservice(
        bookResolver: BookResolver,
        moshi: Moshi
    ): ChapterWebservice = ChapterWebserviceImpl(bookResolver, moshi)

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
     * Creates a [CardDao] instance.
     */
    @Singleton
    @Provides
    fun providesCardDao(db: IgistDb): CardDao = db.cardDao()

    /**
     * Creates a [CardImageDao] instance.
     */
    @Singleton
    @Provides
    fun providesCardImageDao(db: IgistDb): CardImageDao = db.cardImageDao()

    /**
     * Creates a [ChapterDao] instance.
     */
    @Singleton
    @Provides
    fun providesChapterDao(db: IgistDb): ChapterDao = db.chapterDao()

    /**
     * Creates a [ContentFileDao] instance.
     */
    @Singleton
    @Provides
    fun providesContentFileDao(db: IgistDb): ContentFileDao = db.contentFileDao()

    /**
     * Creates a [ContentListDao] instance.
     */
    @Singleton
    @Provides
    fun providesContentListDao(db: IgistDb): ContentListDao = db.contentListDao()

    /**
     * Creates a [StoreCollectionDao] instance.
     */
    @Singleton
    @Provides
    fun providesStoreCollectionDao(db: IgistDb): StoreCollectionDao = db.storeCollectionDao()

    /**
     * Creates a [StoreDepartmentDao] instance.
     */
    @Singleton
    @Provides
    fun providesStoreDepartmentDao(db: IgistDb): StoreDepartmentDao = db.storeDepartmentDao()

    /**
     * Creates a [StoreItemDao] instance.
     */
    @Singleton
    @Provides
    fun providesStoreItemDao(db: IgistDb): StoreItemDao = db.storeItemDao()

    /**
     * Creates an [AppRepository] instance.
     */
    @Singleton
    @Provides
    fun providesAppRepository(
        appDao: ApiDao,
        cardDao: CardDao,
        cardImageDao: CardImageDao,
        contentFileDao: ContentFileDao,
        contentListDao: ContentListDao,
        storeCollectionDao: StoreCollectionDao,
        storeDepartmentDao: StoreDepartmentDao,
        storeItemDao: StoreItemDao,
        appWebservice: AppWebservice,
        sharedPreferences: SharedPreferences
    ): AppRepository = AppRepositoryImpl(
        appDao,
        cardDao,
        cardImageDao,
        contentFileDao,
        contentListDao,
        storeCollectionDao,
        storeDepartmentDao,
        storeItemDao,
        appWebservice,
        sharedPreferences
    )

    /**
     * Creates a [BookRepository] instance.
     */
    @Singleton
    @Provides
    fun providesBookRepository(
        bookDao: BookDao,
        bookWebservice: BookWebservice
    ): BookRepository = BookRepositoryImpl(bookDao, bookWebservice)

    /**
     * Creates a [ChapterRepository] instance.
     */
    @Singleton
    @Provides
    fun providesChapterRepository(
        chapterDao: ChapterDao,
        chapterWebservice: ChapterWebservice
    ): ChapterRepository = ChapterRepositoryImpl(chapterDao, chapterWebservice)

    // endregion Methods

}
