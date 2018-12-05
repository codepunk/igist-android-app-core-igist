/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.punkubator.ui.media.MediaFragment
import com.codepunk.punkubator.widget.TextureViewPanner
import dagger.android.support.AndroidSupportInjection
import io.igist.core.BuildConfig
import io.igist.core.R
import io.igist.core.data.model.Api
import io.igist.core.databinding.FragmentLoadingBinding
import javax.inject.Inject

// region Constants

/**
 * The maximum pool size for creating and storing [MediaPlayer] instances across configuration
 * changes.
 */
private const val MEDIA_FRAGMENT_MAX_POOL_SIZE = 2

/**
 * A tag to use to identify the [MediaFragment].
 */
private const val MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT"

/**
 * A tag used to identify the [MediaPlayer] for playing the "splashy" raw mp4 resource.
 */
private const val SPLASHY_PLAYER = "SPLASHY_PLAYER"

// endregion Constants

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment :
    Fragment(),
    TextureView.SurfaceTextureListener {

    // region Properties

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [LoadingViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

    /**
     * The [LoadingViewModel] instance backing this fragment.
     */
    private val loadingViewModel: LoadingViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(LoadingViewModel::class.java)
    }

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

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

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

        loadingViewModel.apiUpdateData.observe(this, Observer { onApi(it) })

        when (savedInstanceState) {
            null -> loadingViewModel.apiVersion = BuildConfig.API_VERSION
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
        ) { MediaPlayer.create(requireContext(), R.raw.splashy).apply { isLooping = true } }

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
        mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null)
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
     * Reacts to API data updates.
     */
    private fun onApi(update: DataUpdate<Api, Api>) {
        Log.d("LoadingFragment", "onApi: update=$update")
    }

    // endregion Methods

}
