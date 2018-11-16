/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core

import android.app.Activity
import android.app.Application
import android.app.Service
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.igist.core.di.component.DaggerAppComponent
import javax.inject.Inject

class IgistApp :
    Application(),
    HasActivityInjector,
    HasServiceInjector {

    // region Properties

    /**
     * Performs dependency injection on activities.
     */
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    /**
     * Performs dependency injection on services.
     */
    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection for the application and establishes the remote environment
     * for API calls.
     */
    @Suppress("UNRESOLVED")
    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .create(this)
            .inject(this)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasActivityInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into activities.
     */
    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    /**
     * Implementation of [HasServiceInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into services.
     */
    override fun serviceInjector(): AndroidInjector<Service> = serviceDispatchingAndroidInjector

    // endregion Implemented methods
}
