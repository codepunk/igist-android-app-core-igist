/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import io.igist.core.di.scope.ActivityScope
import io.igist.core.ui.launch.LaunchActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * The [Module] used for dependency injection into all activities in the app.
 */
@Module
interface ActivityBuildersModule {

    // region Methods

    /**
     * Contributes an Android injector to [LaunchActivity].
     */
    @Suppress("UNUSED")
    @ActivityScope
    @ContributesAndroidInjector(modules = [LaunchActivityModule::class])
    fun contributeLaunchActivityInjector(): LaunchActivity

    // endregion Methods

}
