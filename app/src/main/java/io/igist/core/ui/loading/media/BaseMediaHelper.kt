/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import androidx.fragment.app.FragmentManager

open class BaseMediaHelper(
    context: Context,
    fragmentManager: FragmentManager
) : MediaHelper(context, fragmentManager) {

    // region Properties

    protected val mediaFragment: MediaFragment
        get() = absMediaFragment as MediaFragment

    // endregion Properties

    // region Inherited methods

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setSurface(holder.surface)

        // TODO TEMP
        mediaPlayer.setOnPreparedListener {
            // TODO Close existing if it is a file
            it.start()
            it.isLooping = true
        }

        val packageName: String = context.packageName
        val uri: Uri = Uri.parse("android.resource://$packageName/raw/splashy")
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
        // END TEMP

        mediaFragment.mediaPlayer = mediaPlayer
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        mediaFragment.mediaPlayer = null
    }

    override fun newMediaFragment(): AbsMediaFragment = MediaFragment()

    // endregion Inherited methods

    // region Nested/inner classes

    class MediaFragment : AbsMediaFragment() {

        // region Properties

        var mediaPlayer: MediaPlayer? = null

        // endregion Properties

    }

    // endregion Nested/inner classes

}
