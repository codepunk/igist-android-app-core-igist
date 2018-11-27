/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import io.igist.core.R
import io.igist.core.databinding.FragmentLoadingBinding
import io.igist.core.ui.loading.media.MediaHelper
import javax.inject.Inject

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment :
    Fragment() {

    // region Properties

    /**
     * A factory for creating the media helper for this fragment.
     */
    @Inject
    lateinit var mediaHelperFactory: MediaHelper.Factory

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

    /**
     * The media helper for this fragment, which supplies API-appropriate media player
     * functionality.
     */
    private val mediaHelper by lazy {
        mediaHelperFactory.newInstance(requireContext(), requireFragmentManager())
    }

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        lifecycle.addObserver(mediaHelper)
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
        binding.surfaceView.holder.addCallback(mediaHelper)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.surfaceView.holder.removeCallback(mediaHelper)
    }

    // endregion Lifecycle methods

}
