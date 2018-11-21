/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.media

import android.annotation.TargetApi
import android.os.Build
import android.os.Build.VERSION_CODES.BASE
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * A retained [Fragment] designed for media playback.
 */
class MediaFragment : Fragment() {

    private val impl : MediaHelperImpl = when (Build.VERSION.SDK_INT) {
        in BASE until P -> BaseMediaHelperImpl()
        else -> PieMediaHelperImpl()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun playMedia(media: String) {
        impl.playMedia(media)
    }

    private interface MediaHelperImpl {

        fun playMedia(media: String)

    }

    private class BaseMediaHelperImpl : MediaHelperImpl {

        override fun playMedia(media: String) {
            // No op
        }

    }

    @TargetApi(P)
    private class PieMediaHelperImpl : MediaHelperImpl {

        override fun playMedia(media: String) {
            // No op
        }

    }
}
