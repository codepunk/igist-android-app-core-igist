/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.base

import android.view.View.OnSystemUiVisibilityChangeListener
import androidx.appcompat.app.AppCompatActivity
import io.igist.core.util.ActivityCompat
import io.igist.core.util.ActivityCompat.FullScreenMode
import io.igist.core.util.ActivityCompat.FullScreenMode.STICKY_IMMERSIVE

/**
 * A base [AppCompatActivity] that keeps the activity in [STICKY_IMMERSIVE] mode.
 */
abstract class StickyImmersiveActivity :
    AppCompatActivity(),
    OnSystemUiVisibilityChangeListener
{

    /**
     * Begins listening for system UI visibility changes.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.decorView.setOnSystemUiVisibilityChangeListener(this)
    }

    // region Inherited methods

    /**
     * Hides system UI according to the supplied [FullScreenMode].
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ActivityCompat.setFullScreenMode(this, STICKY_IMMERSIVE)
        }
    }

    // endregion Inherited methods

    /**
     * Ensures that system UI is properly set.
     */
    override fun onSystemUiVisibilityChange(visibility: Int) {
        ActivityCompat.ensureFullScreenMode(this, visibility, STICKY_IMMERSIVE)
    }
}
