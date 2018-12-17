/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.OnBuildAlertDialogListener
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import dagger.android.support.AndroidSupportInjection
import io.igist.core.R
import javax.inject.Inject

// region Constants

/**
 * The request code for the preparing launch dialog fragment.
 */
const val PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE: Int = 1

// endregion Constants

/**
 * Abstract loading fragment with some common functionality.
 */
abstract class AbsLoadingFragment :
    Fragment(),
    OnBuildAlertDialogListener {

    // region Properties

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [LoadingViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * The [LoadingViewModel] instance backing this fragment.
     */
    protected val loadingViewModel: LoadingViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(LoadingViewModel::class.java)
    }

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        loadingViewModel.loadingUpdate.observe(this, Observer { onLoadingUpdate(it) })
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Implementation of [OnBuildAlertDialogListener]. Builds the alert dialog.
     */
    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        when (fragment.targetRequestCode) {
            PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE -> {
                builder
                    .setTitle(R.string.loading_dialog_preparing_launch_title)
                    .setMessage(R.string.loading_dialog_preparing_launch_message)
                    .setPositiveButton(R.string.app_retry, fragment)
                    .setNegativeButton(R.string.app_quit, fragment)
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Shows a dialog fragment with the supplied [tag] and [requestCode]. If another fragment
     * exists with the same [tag], the request is ignored.
     */
    protected fun showAlert(tag: String, requestCode: Int) {
        requireFragmentManager().findFragmentByTag(tag)
            ?: AlertDialogFragment.show(tag, this, requestCode)
    }

    /**
     * Reacts to loading LiveData updates.
     */
    protected open fun onLoadingUpdate(update: DataUpdate<Int, Boolean>) { // No op
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A fragment tag for the dummy book dialog fragment.
         */
        @JvmStatic
        protected val PREPARING_LAUNCH_DIALOG_FRAGMENT_TAG: String =
            AbsLoadingFragment::class.java.name + ".PREPARING_LAUNCH_DIALOG_FRAGMENT"

        // endregion Properties

    }

    // endregion Companion object

}
