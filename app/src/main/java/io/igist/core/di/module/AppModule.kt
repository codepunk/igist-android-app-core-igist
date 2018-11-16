/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.igist.core.IgistApp
import io.igist.core.di.qualifier.ApplicationContext
import javax.inject.Singleton

/*
 * Author(s): Scott Slater
 */

@Module
object AppModule {

    // region Methods

    /**
     * Provides the application-level [Context].
     */
    @JvmStatic
    @Provides
    @Singleton
    @ApplicationContext
    fun providesContext(app: IgistApp): Context = app

    /**
     * Provides the default [SharedPreferences] for the app.
     */
    @JvmStatic
    @Provides
    @Singleton
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    // endregion Methods

}
