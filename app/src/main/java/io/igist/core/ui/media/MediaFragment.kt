/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.media

import android.os.Bundle
import androidx.fragment.app.Fragment

class MediaFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    // region Companion object

    companion object {

        // region Methods

        fun newInstance(poolSize: Int = 1): MediaFragment =
            MediaFragment().apply {
                arguments = Bundle().apply {

                }
            }

        // endregion Methods

    }

    // endregion Companion object

}
