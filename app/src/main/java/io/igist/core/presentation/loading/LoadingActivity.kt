/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import io.igist.core.BuildConfig.ACTION_SELECT_BOOK
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.R
import io.igist.core.databinding.ActivityLoadingBinding
import io.igist.core.presentation.base.StickyImmersiveActivity
import javax.inject.Inject

private const val SELECT_BOOK_REQUEST_CODE = 1

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
     * The application [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

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

        val bookId =
            sharedPreferences.getLong(PREF_KEY_CURRENT_BOOK_ID, -1L)
        if (bookId < 0L) {
            startActivityForResult(Intent(ACTION_SELECT_BOOK), SELECT_BOOK_REQUEST_CODE)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loading)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Processes the result of SelectBookActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_BOOK_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {

                    }
                    else -> finish()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [HasSupportFragmentInjector]. Returns a
     * [DispatchingAndroidInjector] that injects dependencies into fragments.
     */
    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    // endregion Implemented methods

}
