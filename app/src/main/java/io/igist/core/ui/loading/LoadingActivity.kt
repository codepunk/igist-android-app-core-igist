/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.app.Activity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import io.igist.core.R
import io.igist.core.databinding.ActivityLoadingBinding
import io.igist.core.ui.base.StickyImmersiveActivity
import javax.inject.Inject

/**
 * An [Activity] that manages launch- and onboarding-related tasks and fragments.
 */
class LoadingActivity :
    StickyImmersiveActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /**
     * The binding for this activity.
     */
    private lateinit var binding: ActivityLoadingBinding

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets up [Fabric], performs dependency injection and sets up the content view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loading)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

}
