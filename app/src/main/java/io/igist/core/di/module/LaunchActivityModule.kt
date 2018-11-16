/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import io.igist.core.di.scope.FragmentScope
import io.igist.core.ui.launch.LaunchActivity
import io.igist.core.ui.launch.LoadingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * A [Module] for injecting dependencies into [LaunchActivity].
 */
@Module
abstract class LaunchActivityModule {

    // region Methods

    /**
     * Contributes an AndroidInjector to [LoadingFragment].
     */
    @Suppress("UNUSED")
    @FragmentScope
    @ContributesAndroidInjector(modules = [LoadingFragmentModule::class])
    abstract fun contributeLoadingFragmentInjector(): LoadingFragment

    // endregion Methods

    // region Companion object

    @Module
    companion object {

        /*
        @JvmStatic
        @Provides
        @ActivityScope
        fun providesSomething(): String = "Hello"
        */

    }

    // endregion Companion object

}
