/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import androidx.fragment.app.FragmentManager
import io.igist.core.presentation.loading.LoadingFragment
import dagger.Module
import dagger.Provides
import io.igist.core.di.scope.FragmentScope

/**
 * A [Module] for injecting dependencies into [LoadingFragment].
 */
@Module
class LoadingFragmentModule {

    // region Methods

    /**
     * Provides the [LoadingFragment]-level [FragmentManager].
     */
    @Provides
    @FragmentScope
    fun providesFragmentManager(fragment: LoadingFragment): FragmentManager =
        fragment.requireFragmentManager()

    // endregion Methods

}
