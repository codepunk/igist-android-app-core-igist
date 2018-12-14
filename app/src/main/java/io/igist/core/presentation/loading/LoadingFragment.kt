/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.punkubator.ui.media.MediaFragment
import com.codepunk.punkubator.widget.TextureViewPanner
import io.igist.core.BuildConfig.DEBUG
import io.igist.core.BuildConfig.KEY_DESCRIPTION
import io.igist.core.R
import io.igist.core.databinding.FragmentLoadingBinding
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.ResultMessage
import io.igist.core.domain.session.AppSessionManager
import java.io.IOException
import javax.inject.Inject

// region Constants

/**
 * The maximum pool size for creating and storing [MediaPlayer] instances across configuration
 * changes.
 */
private const val MEDIA_FRAGMENT_MAX_POOL_SIZE = 2

/**
 * The tag to use to identify the [MediaFragment].
 */
private const val MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT"

/**
 * The tag used to identify the [MediaPlayer] for playing the "splashy" raw mp4 resource.
 */
private const val SPLASHY_PLAYER = "SPLASHY_PLAYER"

// endregion Constants

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment :
    AbsLoadingFragment(),
    TextureView.SurfaceTextureListener {

    // region Properties

    /**
     * The [AppSessionManager] holding application-level session data.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var appSessionManager: AppSessionManager

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

    /**
     * The retained media fragment.
     */
    private lateinit var mediaFragment: MediaFragment

    /**
     * The [Surface] on which media will be played.
     */
    private var surface: Surface? = null

    /**
     * A [TextureViewPanner] for panning the video when the video doesn't fit nicely into
     * the TextureView's aspect ratio.
     */
    private lateinit var textureViewPanner: TextureViewPanner

    /**
     * The default visibility for [FragmentLoadingBinding.progressDescriptionTxt].
     */
    // TODO Set this based on a developer options setting
    private var defaultProgressDescriptionVisibility: Int = View.VISIBLE

    // endregion Properties

    // region Lifecycle methods

    /**
     * Ensures that we have a valid [MediaFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaFragment = requireFragmentManager().let { fm ->
            fm.findFragmentByTag(MEDIA_FRAGMENT_TAG) as? MediaFragment
                ?: MediaFragment.newInstance(MEDIA_FRAGMENT_MAX_POOL_SIZE).apply {
                    fm.beginTransaction()
                        .add(this, MEDIA_FRAGMENT_TAG)
                        .commit()
                }
        }.apply {
            lifecycle.addObserver(this)
        }
    }

    /**
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_loading,
            container,
            false
        )
        return binding.root
    }

    /**
     * Initializes the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureViewPanner = TextureViewPanner.Builder(binding.textureView).build()
        binding.textureView.surfaceTextureListener = this
        defaultProgressDescriptionVisibility = when (DEBUG) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }

    /**
     * Cleans up the loading fragment.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        textureViewPanner.release()

        // Post the below code to ensure onSurfaceTextureDestroyed is called
        binding.textureView.post {
            binding.textureView.surfaceTextureListener = null
        }
    }

    /**
     * Removes this lifecycle owner from [MediaFragment].
     */
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(mediaFragment)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Processes dialog fragment result(s).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE -> {

            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Implementation of [TextureView.SurfaceTextureListener]. Plays the appropriate video once
     * the surface texture is available.
     */
    @SuppressLint("Recycle")
    override fun onSurfaceTextureAvailable(
        texture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        val mediaPlayer: MediaPlayer = mediaFragment.mediaPlayers.obtain(
            SPLASHY_PLAYER
        ) {
            MediaPlayer.create(requireContext(), R.raw.splashy).apply {
                isLooping = true
            }
        }

        surface = Surface(texture)
        mediaPlayer.setSurface(surface)

        mediaPlayer.setOnVideoSizeChangedListener { _, videoWidth, videoHeight ->
            binding.textureView.setContentSize(videoWidth, videoHeight)
        }

        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    /**
     * Implementation of [TextureView.SurfaceTextureListener]. Updates the texture view panner.
     */
    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
        textureViewPanner.update()
    }

    /**
     * Implementation of [TextureView.SurfaceTextureListener]. Detaches from the media player.
     */
    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        /* TODO Causes IllegalStateException
        mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null) */
        surface?.release()
        surface = null
        return true
    }

    /**
     * Implementation of [TextureView.SurfaceTextureListener]. Unused.
     */
    override fun onSurfaceTextureSizeChanged(
        texture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        // No op
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Reacts to loading LiveData updates.
     */
    override fun onLoadingUpdate(update: DataUpdate<Int, Boolean>) {
        Log.d("LoadingFragment", "onLoadingUpdate: update=$update")
        when (update) {
            is ProgressUpdate -> {
                binding.progressDescriptionTxt.visibility = defaultProgressDescriptionVisibility
                binding.progressDescriptionTxt.text = update.data?.getString(KEY_DESCRIPTION)

                val progress: Int = update.progress.getOrElse(0) { 0 } ?: 0
                val max: Int = update.progress.getOrElse(1) { 0 } ?: 0
                binding.loadingProgress.visibility = View.VISIBLE
                binding.loadingProgress.progress = progress
                binding.loadingProgress.isIndeterminate = (max == 0)
            }
            is FailureUpdate -> {
                val e: Exception? = update.e
                when (e) {
                    is IOException -> showAlert(
                        PREPARING_LAUNCH_DIALOG_FRAGMENT_TAG,
                        PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE
                    )
                    is IgistException -> {
                        when (e.resultMessage) {
                            ResultMessage.BETA_KEY_REQUIRED -> {
                                Navigation.findNavController(
                                    requireActivity(),
                                    R.id.loading_nav_fragment
                                ).navigate(R.id.action_loading_to_beta_key)
                            }
                            ResultMessage.BETA_KEY_REQUIRED -> {
                            }
                        }
                    }
                }
            }
        }
    }

}
