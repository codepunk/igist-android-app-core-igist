/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading.media

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.SurfaceHolder
import androidx.fragment.app.FragmentManager
import androidx.media2.DataSourceDesc2
import androidx.media2.MediaPlayer2
import androidx.media2.MediaPlayer2.CALL_COMPLETED_PREPARE
import androidx.media2.MediaPlayer2.CALL_COMPLETED_SET_DATA_SOURCE
import androidx.media2.UriDataSourceDesc2
import java.util.concurrent.Executors

@TargetApi(Build.VERSION_CODES.P)
open class PieMediaHelper(
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
        val mediaPlayer = MediaPlayer2.create(context)
        mediaPlayer.setSurface(holder.surface)

        // TODO TEMP
        mediaPlayer.setEventCallback(
            Executors.newSingleThreadExecutor(),
            object : MediaPlayer2.EventCallback() {
                override fun onCallCompleted(
                    mp: MediaPlayer2?,
                    dsd: DataSourceDesc2?,
                    what: Int,
                    status: Int
                ) {
                    @Suppress("SwitchIntDef")
                    when (what) {
                        CALL_COMPLETED_SET_DATA_SOURCE -> mediaPlayer.prepare()
                        CALL_COMPLETED_PREPARE -> {
                            // TODO Close existing if it is a file
                            mediaPlayer.play()
                            mediaPlayer.loopCurrent(true)
                        }
                    }
                }
            }
        )
        val packageName: String = context.packageName
        val uri: Uri = Uri.parse("android.resource://$packageName/raw/splashy")
        val dsd = UriDataSourceDesc2.Builder(context, uri).build()
        mediaPlayer.setDataSource(dsd)
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

        var mediaPlayer: MediaPlayer2? = null

        // endregion Properties
    }

    // endregion Nested/inner classes

}
