/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.component

import io.igist.core.di.module.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.igist.core.IgistApp
import javax.inject.Singleton

/**
 * A [Component] for dependency injection into the application.
 */
@Singleton
@Component(
    modules = [
        ActivityBuildersModule::class,
        AndroidSupportInjectionModule::class,
        AppModule::class,
        DataModule::class,
        ViewModelModule::class,
        ServiceBuildersModule::class
    ]
)
interface AppComponent : AndroidInjector<IgistApp> {

    // region Methods

    /**
     * Returns a new [UserComponent.Builder] for building new
     * [UserComponent] instances.
     */
    fun userComponentBuilder(): UserComponent.Builder

    // endregion Methods

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [AppComponent].
     */
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<IgistApp>() {

        // region Methods

        /**
         * Builds an instance of type [AppComponent].
         */
        abstract override fun build(): AppComponent

        // endregion Methods

    }

    // endregion Nested/inner classes

}
