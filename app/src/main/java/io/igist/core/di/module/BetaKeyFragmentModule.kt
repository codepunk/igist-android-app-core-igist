/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import androidx.fragment.app.FragmentManager
import dagger.Module
import dagger.Provides
import io.igist.core.di.scope.FragmentScope
import io.igist.core.presentation.loading.BetaKeyFragment

/**
 * A [Module] for injecting dependencies into [BetaKeyFragment].
 */
@Module
class BetaKeyFragmentModule {

    // region Methods

    /**
     * Provides the [BetaKeyFragment]-level [FragmentManager].
     */
    @Provides
    @FragmentScope
    fun providesFragmentManager(fragment: BetaKeyFragment): FragmentManager =
        fragment.requireFragmentManager()

    // endregion Methods

}
