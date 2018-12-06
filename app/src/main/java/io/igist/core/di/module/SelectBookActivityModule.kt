/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.igist.core.di.scope.FragmentScope
import io.igist.core.presentation.loading.SelectBookActivity
import io.igist.core.presentation.loading.SelectBookFragment

/**
 * A [Module] for injecting dependencies into [SelectBookActivity].
 */
@Module
abstract class SelectBookActivityModule {

    // region Methods

    /**
     * Contributes an AndroidInjector to [SelectBookFragment].
     */
    @Suppress("UNUSED")
    @FragmentScope
    @ContributesAndroidInjector(modules = [SelectBookFragmentModule::class])
    abstract fun contributeSelectBookFragmentInjector(): SelectBookFragment

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
