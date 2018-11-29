/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.igist.core.data.task.DataUpdate
import dagger.android.support.AndroidSupportInjection
import io.igist.core.R
import io.igist.core.data.model.Api
import io.igist.core.databinding.FragmentLoadingBinding
import javax.inject.Inject

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
                ?: MediaFragment().apply {
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
        binding.surfaceView.holder.removeCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(mediaFragment)
    }

    // endregion Lifecycle methods

    // region Implemented methods

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(this::class.java.simpleName, "surfaceCreated: holder=$holder")

        mediaFragment.mediaPlayer?.setSurface(holder.surface) ?: run {
            MediaPlayer.create(requireContext(), R.raw.splashy).apply {
                mediaFragment.mediaPlayer = this
                setSurface(holder.surface)
                isLooping = true
                start()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(this::class.java.simpleName, "surfaceDestroyed: holder=$holder")

        // Do not destroy the media player because we want it to live through configuration changes
    }

    // endregion Implemented methods

    // region Methods

    private fun onApi(update: DataUpdate<Void, Api>) {
        Log.d("LoadingFragment", "onApi: update=$update")
    }

    // endregion Methods

    // region Companion object

    companion object {

        @JvmStatic
        val MEDIA_FRAGMENT_TAG = "${LoadingFragment::class.java.name}.MEDIA_FRAGMENT"

    }

    // endregion Companion object

    // region Nested/inner classes

    class MediaFragment :
        Fragment(),
        LifecycleObserver {

        // region Properties

        var mediaPlayer: MediaPlayer? = null

        // endregion Properties

        // region Lifecycle methods

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
        }

        // endregion Lifecycle methods

        // region Methods

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun observedOnStart() {
            Log.d(LoadingFragment::class.java.simpleName, "observedOnStart")
            mediaPlayer?.start()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun observedOnStop() {
            Log.d(LoadingFragment::class.java.simpleName, "observedOnStop")
            mediaPlayer?.pause()
        }

        // endregion Methods

    }

    // endregion Nested/inner classes

}
