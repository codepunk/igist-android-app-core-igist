/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.igist.core.di.scope.FragmentScope
import io.igist.core.presentation.loading.BetaKeyFragment
import io.igist.core.presentation.loading.LoadingActivity
import io.igist.core.presentation.loading.LoadingFragment

/**
 * A [Module] for injecting dependencies into [LoadingActivity].
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

    /**
     * Contributes an AndroidInjector to [BetaKeyFragment].
     */
    @Suppress("UNUSED")
    @FragmentScope
    @ContributesAndroidInjector(modules = [LoadingFragmentModule::class])
    abstract fun contributeBetaKeyFragmentInjector(): BetaKeyFragment

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
