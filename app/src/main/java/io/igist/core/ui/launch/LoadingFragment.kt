/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.launch

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

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment : Fragment() {

    // region Properties

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

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

}
