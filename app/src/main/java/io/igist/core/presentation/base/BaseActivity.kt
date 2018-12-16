/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.base

import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

abstract class BaseActivity : AppCompatActivity() {

    // region Properties

    /**
     * A weak set of [OnBackPressedListener]s.
     */
    private val onBackPressedListeners: HashSet<WeakReference<OnBackPressedListener>> = HashSet()

    // endregion Properties

    // region Inherited methods

    /**
     * Called when the user presses the back button.
     */
    override fun onBackPressed() {
        var handled = false
        onBackPressedListeners.forEach {
            handled = handled || (it.get()?.onBackPressed(this) ?: false)
        }
        if (!handled) {
            super.onBackPressed()
        }
    }

    // endregion Inherited methods

    // region Methods

    fun registerOnBackPressedListener(listener: OnBackPressedListener) {
        val found =
            onBackPressedListeners.find { it.get() == listener }
        if (found == null) {
            onBackPressedListeners.add(WeakReference(listener))
        }
    }

    fun unregisterOnBackPressedListener(listener: OnBackPressedListener) {
        val found =
            onBackPressedListeners.find { it.get() == listener }
        found?.run { onBackPressedListeners.remove(this) }
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * Interface used to listen for when the user presses the back button.
     */
    interface OnBackPressedListener {

        // region Methods

        /**
         * Called when the user presses the back button.
         */
        fun onBackPressed(activity: BaseActivity): Boolean

        // endregion Methods
    }

    // endregion Nested/inner classes

}
