/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment

open class AlertDialogFragment :
    AppCompatDialogFragment(),
    OnClickListener {

    // region Properties

    protected var resultCode: Int = RESULT_CANCELED

    // endregion Properties

    // region Inherited methods

    /**
     * Builds the [Dialog] that confirms that the user wishes to disable developer options.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        onBuildAlertDialog(builder)
        (targetFragment as? OnAlertDialogBuildListener)?.onBuildAlertDialog(this, builder)
        return builder.create()
    }

    /**
     * Sets [resultCode] to [Activity.RESULT_CANCELED] so it can be passed to the target fragment
     * when the dialog is dismissed.
     */
    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        resultCode = Activity.RESULT_CANCELED
    }

    /**
     * Called when the dialog is dismissing.
     */
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        // Don't notify target fragment if the dialog is not attached. This will catch the
        // difference between a dialog dismissing due to configuration change vs. a user action.
        if (isAdded) {
            targetFragment?.run {
                onNotifyTargetFragment(this)
            }
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    override fun onClick(dialog: DialogInterface?, which: Int) {
        resultCode = when (which) {
            BUTTON_POSITIVE -> RESULT_POSITIVE
            BUTTON_NEGATIVE -> RESULT_NEGATIVE
            BUTTON_NEUTRAL -> RESULT_NEUTRAL
            else -> return
        }
    }

    // endregion Implemented methods

    // region Methods

    open fun onBuildAlertDialog(builder: AlertDialog.Builder) {
        // No op
    }

    /**
     * Calls [Fragment.onActivityResult] on the supplied [targetFragment] with the appropriate
     * request code and result code. Descendants of this fragment can include other data in
     * the call (via the data argument to onActivityResult) if needed.
     */
    open fun onNotifyTargetFragment(targetFragment: Fragment) {
        targetFragment.onActivityResult(targetRequestCode, resultCode, null)
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        @JvmStatic
        val RESULT_POSITIVE: Int = Activity.RESULT_OK

        @JvmStatic
        val RESULT_NEGATIVE: Int = Activity.RESULT_FIRST_USER

        @JvmStatic
        val RESULT_NEUTRAL: Int = Activity.RESULT_FIRST_USER + 1

        @JvmStatic
        val RESULT_CANCELED: Int = Activity.RESULT_CANCELED

        // endregion Properties

        // region Methods

        fun show(
            tag: String,
            targetFragment: Fragment,
            requestCode: Int,
            arguments: Bundle? = null
        ): AlertDialogFragment = AlertDialogFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            this.arguments = arguments
            show(targetFragment.requireFragmentManager(), tag)
        }

        // endregion Methods

    }

    // endregion Companion object

    // region Nested/inner classes

    interface OnAlertDialogBuildListener {
        fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder)
    }

    // endregion Nested/inner classes

}
