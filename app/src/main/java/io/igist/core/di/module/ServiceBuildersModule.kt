/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.module

import io.igist.core.di.scope.ServiceScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * The [Module] used for dependency injection into all activities in the app.
 */
@Module
interface ServiceBuildersModule {

    // region Methods

    /*
    /**
     * Contributes an AndroidInjector to [AuthenticatorService].
     */
    @ServiceScope
    @ContributesAndroidInjector //(modules = [AuthenticatorServiceModule::class])
    fun contributeAuthenticatorServiceInjector(): AuthenticatorService
    */

    // endregion methods

}
