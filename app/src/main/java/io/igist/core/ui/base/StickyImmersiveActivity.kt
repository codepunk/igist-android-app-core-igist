/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.base

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import androidx.appcompat.app.AppCompatActivity
import io.igist.core.util.ActivityCompat
import io.igist.core.util.ActivityCompat.FullScreenMode

open class StickyImmersiveActivity :
    AppCompatActivity(),
    OnSystemUiVisibilityChangeListener
{

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
            ActivityCompat.hideSystemUI(this, FullScreenMode.STICKY_IMMERSIVE)
        }
    }

    // endregion Inherited methods

    /**
     * Ensures that system UI is up to date.
     */
    override fun onSystemUiVisibilityChange(visibility: Int) {
        onWindowFocusChanged(true)
    }
}
