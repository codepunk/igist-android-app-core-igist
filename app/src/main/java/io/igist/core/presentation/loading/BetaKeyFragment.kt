/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.OnBuildAlertDialogListener
import dagger.android.support.AndroidSupportInjection
import io.igist.core.R
import io.igist.core.databinding.FragmentBetaKeyBinding
import javax.inject.Inject

class BetaKeyFragment :
    Fragment(),
    OnBuildAlertDialogListener {

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
    private lateinit var binding: FragmentBetaKeyBinding

    /**
     * The [LoadingViewModel] instance backing this fragment.
     */
    private val loadingViewModel: LoadingViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(LoadingViewModel::class.java)
    }

    // endregion Properties

    // region Lifecycle methods

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
            R.layout.fragment_beta_key,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Implemented methods

    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        TODO("not implemented")
    }

    // endregion Implemented methods

}
