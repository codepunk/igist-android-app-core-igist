/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
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
    SurfaceHolder.Callback {

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

        binding.surfaceView.holder.addCallback(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // If we're changing configurations and the view is being destroyed, surfaceDestroyed
        // won't get called if we remove the callback here; call it manually instead.
        surfaceDestroyed(binding.surfaceView.holder)
        binding.surfaceView.holder.removeCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(mediaFragment)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    override fun surfaceCreated(holder: SurfaceHolder) {
        val mediaPlayer: MediaPlayer = mediaFragment.mediaPlayers.obtain(
            SPLASHY_PLAYER
        ) { MediaPlayer.create(requireContext(), R.raw.splashy) }

        mediaPlayer.setSurface(holder.surface)

        mediaPlayer.setOnPreparedListener {
            it.isLooping = true
            it.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null)
    }

    // endregion Implemented methods

    // region Methods

    private fun onApi(update: DataUpdate<Void, Api>) {
        Log.d("LoadingFragment", "onApi: update=$update")
    }

    // endregion Methods

}
