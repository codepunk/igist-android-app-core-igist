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
import io.igist.core.ui.media.MediaFragment

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment : Fragment() {

    // region Properties

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

    private lateinit var mediaFragment: MediaFragment

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
     * Creates (or finds) the media fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaFragment = requireFragmentManager().let { fm ->
            fm.findFragmentByTag(MEDIA_FRAGMENT_TAG) as? MediaFragment ?: MediaFragment().apply {
                fm.beginTransaction()
                    .add(this, MEDIA_FRAGMENT_TAG)
                    .commit()
            }
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

    // endregion Lifecycle methods

    // region Companion object

    companion object {

        val MEDIA_FRAGMENT_TAG = "${LoadingFragment::class.java.name}.MEDIA_FRAGMENT"

    }

    // endregion Companion object

}
