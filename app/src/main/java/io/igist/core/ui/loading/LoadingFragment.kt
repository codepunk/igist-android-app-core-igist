/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.punkubator.ui.media.MediaFragment
import com.igist.core.data.task.DataUpdate
import dagger.android.support.AndroidSupportInjection
import io.igist.core.R
import io.igist.core.data.model.Api
import io.igist.core.databinding.FragmentLoadingBinding
import javax.inject.Inject

// region Constants

private const val MEDIA_FRAGMENT_MAX_POOL_SIZE = 2

private const val MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT"

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
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(LoadingViewModel::class.java)
    }

    /**
     * The retained media fragment.
     */
    lateinit var mediaFragment: MediaFragment

    var surface: Surface? = null

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

        loadingViewModel.apiDataUpdate.observe(this, Observer { onApi(it) })

        when (savedInstanceState) {
            null -> loadingViewModel.getApi()
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
        binding.textureView.surfaceTextureListener = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Post the below code, or else onSurfaceTextureDestroyed won't get called
        binding.textureView.post {
            binding.textureView.surfaceTextureListener = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(mediaFragment)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    @SuppressLint("Recycle")
    override fun onSurfaceTextureAvailable(
        texture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        surface = Surface(texture)

        val mediaPlayer: MediaPlayer = mediaFragment.mediaPlayers.obtain(
            SPLASHY_PLAYER
        ) { MediaPlayer.create(requireContext(), R.raw.splashy).apply { isLooping = true } }

        mediaPlayer.setSurface(surface)

        mediaPlayer.also { //setOnPreparedListener {

            // TODO TEMP Adjust video
            // One of them will be 1.0f
            val vWidth = it.videoWidth
            val vHeight = it.videoHeight
            val tWidth = binding.textureView.width
            val tHeight = binding.textureView.height
            val xScale = tWidth.toFloat() / vWidth
            val yScale = tHeight.toFloat() / vHeight
            val scale = Math.max(xScale, yScale)

            val sx = scale / xScale
            val sy = scale / yScale
            val matrix = Matrix()
            matrix.setScale(sx, sy)

            val tx = (tWidth * (1 - sx)) / 2.0f
            val ty = (tHeight * (1 - sy)) / 2.0f
            matrix.postTranslate(tx, ty)

            binding.textureView.setTransform(matrix)


            Log.d(
                "LoadingFragment",
                "onSurfaceTextureAvailable: video=($vWidth x $vHeight), textureView=($tWidth x $tHeight), xScale=$xScale, yScale=$yScale, scale=$scale"
            )
            // END TEMP

            if (!it.isPlaying) {
                it.start()
            }
        }

    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
        // No op
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        Log.d(
            "LoadingFragment",
            "onSurfaceTextureDestroyed"
        )
        mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null)
        surface?.release()
        surface = null
        return false
    }

    override fun onSurfaceTextureSizeChanged(
        texture: SurfaceTexture,
        width: Int,
        height: Int
    ) {

    }

    // endregion Implemented methods

    // region Methods

    private fun onApi(update: DataUpdate<Void, Api>) {
        Log.d("LoadingFragment", "onApi: update=$update")
    }

    // endregion Methods

}
