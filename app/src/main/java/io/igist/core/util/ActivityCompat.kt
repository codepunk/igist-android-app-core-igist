/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.util

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Build.VERSION_CODES.*
import android.view.View

/**
 * Compatibility class that allows for immersive modes on devices running Kit Kat or later.
 * For these modes (i.e. SYSTEM_UI_FLAG_IMMERSIVE, SYSTEM_UI_FLAG_IMMERSIVE_STICKY etc.),
 * earlier devices will simply enter SYSTEM_UI_FLAG_LOW_PROFILE mode.
 */
object ActivityCompat {

    // region Properties

    /**
     * Class that implements Activity compatibility methods.
     */
    private val IMPL: ActivityCompatImpl = when (Build.VERSION.SDK_INT) {
        in BASE until KITKAT -> BaseActivityCompatImpl()
        else -> KitKatActivityCompatImpl()
    }

    // endregion Properties

    // region Methods

    /**
     * Hides the system UI in an api-independent manner.
     */
    fun hideSystemUI(activity: Activity, mode: FullScreenMode) {
        IMPL.hideSystemUI(activity, mode)
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * An enum class that describes a fullscreen mode.
     */
    enum class FullScreenMode {

        /**
         * Do not implement a fullscreen mode.
         */
        NONE,

        /**
         * Implement a "lean back" full screen mode.
         */
        LEAN_BACK,

        /**
         * Implement an "immersive" full screen mode.
         */
        IMMERSIVE,

        /**
         * Implement a "sticky immersive" full screen mode.
         */
        STICKY_IMMERSIVE
    }

    /**
     * An interface that allows for api-independent fullscreen mode handling.
     */
    private interface ActivityCompatImpl {

        fun hideSystemUI(activity: Activity, mode: FullScreenMode)

    }

    /**
     * A base implementation for hiding system UI elements.
     */
    private class BaseActivityCompatImpl : ActivityCompatImpl {

        /**
         * Hides system UI elements based on the supplied [mode].
         */
        override fun hideSystemUI(activity: Activity, mode: FullScreenMode) {
            val desiredSystemUiVisibility = when (mode) {
                FullScreenMode.NONE -> 0
                else -> View.SYSTEM_UI_FLAG_LOW_PROFILE
            }
            if (activity.window.decorView.systemUiVisibility != desiredSystemUiVisibility) {
                activity.window.decorView.systemUiVisibility = desiredSystemUiVisibility
            }
        }

    }

    /**
     * A KitKat implementation for hiding system UI elements.
     */
    @TargetApi(KITKAT)
    private class KitKatActivityCompatImpl : ActivityCompatImpl {

        /**
         * Hides system UI elements based on the supplied [mode].
         */
        override fun hideSystemUI(activity: Activity, mode: FullScreenMode) {
            val immersiveFlag = when (mode) {
                FullScreenMode.NONE -> 0
                FullScreenMode.LEAN_BACK -> 0
                FullScreenMode.IMMERSIVE -> View.SYSTEM_UI_FLAG_IMMERSIVE
                FullScreenMode.STICKY_IMMERSIVE -> View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }

            val otherFlags = when (mode) {
                FullScreenMode.NONE -> 0
                else ->
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            // Hide the nav bar and status bar
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            }

            val desiredSystemUiVisibility = immersiveFlag or otherFlags
            if (activity.window.decorView.systemUiVisibility != desiredSystemUiVisibility) {
                activity.window.decorView.systemUiVisibility = desiredSystemUiVisibility
            }
        }

    }

    // endregion Nested/inner classes

}
