/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import io.igist.core.di.provider.InjectingViewModelFactory

/**
 * A [Module] for injecting [ViewModel] instances and the [ViewModelProvider.Factory] that will
 * be used to create them.
 */
@Suppress("UNUSED")
@Module
interface ViewModelModule {

    // region Methods

    /*
    /**
     * Binds an instance of [MainViewModel] to this [Module] for dependency injection.
     */
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel
    */

    /**
     * Binds an instance of [InjectingViewModelFactory] to this [Module] for dependency injection.
     */
    @Binds
    fun bindViewModelFactory(factory: InjectingViewModelFactory): ViewModelProvider.Factory

    // endregion Methods

}
