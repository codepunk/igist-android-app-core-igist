/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import io.igist.core.di.scope.ActivityScope
import io.igist.core.ui.loading.LoadingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.igist.core.ui.loading.SelectBookActivity

/**
 * The [Module] used for dependency injection into all activities in the app.
 */
@Module
interface ActivityBuildersModule {

    // region Methods

    /**
     * Contributes an Android injector to [LoadingActivity].
     */
    @Suppress("UNUSED")
    @ActivityScope
    @ContributesAndroidInjector(modules = [SelectBookActivityModule::class])
    fun contributeSelectBookActivityInjector(): SelectBookActivity

    /**
     * Contributes an Android injector to [LoadingActivity].
     */
    @Suppress("UNUSED")
    @ActivityScope
    @ContributesAndroidInjector(modules = [LaunchActivityModule::class])
    fun contributeLaunchActivityInjector(): LoadingActivity

    // endregion Methods

}
