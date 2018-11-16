/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import io.igist.core.R
import io.igist.core.databinding.ActivityLaunchBinding
import javax.inject.Inject

class LaunchActivity :
    AppCompatActivity(),
    HasSupportFragmentInjector {

    // region Properties

    /**
     * Performs dependency injection on fragments.
     */
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var binding: ActivityLaunchBinding

    // endregion Properties

    // region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_launch)
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
