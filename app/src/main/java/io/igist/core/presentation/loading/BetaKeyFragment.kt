/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.OnBuildAlertDialogListener
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.R
import io.igist.core.databinding.FragmentBetaKeyBinding
import io.igist.core.domain.exception.IgistException

// region Constants

/**
 * The request code for a bad beta key dialog fragment.
 */
private const val BAD_KEY_DIALOG_FRAGMENT_REQUEST_CODE: Int = 2

// endregion Constants

class BetaKeyFragment :
    AbsLoadingFragment(),
    OnClickListener,
    OnBuildAlertDialogListener {

    // region Properties

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentBetaKeyBinding

    // endregion Properties

    // region Lifecycle methods

    /**
     * Begins observing beta key updates.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        loadingViewModel.liveBetaKey.observe(this, Observer { onBetaKeyUpdate(it) })
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

    /**
     * Initializes the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Workaround addressing a flaw in Android's fragment transition framework: moves this
        // fragment's view higher when it is animating in.
        ViewCompat.setTranslationZ(view, 0.1f)

        binding.submitBtn.setOnClickListener(this)
    }

    // endregion Lifecycle methods

    // region Inherited methods

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
     * Submits the entered beta key.
     */
    override fun onClick(v: View?) {
        when (v) {
            // TODO Validatinators!
            binding.submitBtn -> {
                binding.betaKeyEdit.isEnabled = false
                binding.submitBtn.isEnabled = false
                binding.betaKeyProgress.show()
                loadingViewModel.submitBetaKey(binding.betaKeyEdit.text?.toString())
            }
        }
    }

    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        when (fragment.targetRequestCode) {
            BAD_KEY_DIALOG_FRAGMENT_REQUEST_CODE -> {
                builder
                    .setTitle(R.string.beta_key_dialog_bad_key_title)
                    .setMessage(R.string.beta_key_dialog_bad_key_message)
                    .setPositiveButton(android.R.string.ok, fragment)
            }
            else -> {
                super.onBuildAlertDialog(fragment, builder)
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Reacts to loading LiveData updates.
     */
    // TODO This is getting complicated. What's the easiest version?
    // Also, on "back button"/cancel, should just exit the app
    override fun onLoadingUpdate(update: DataUpdate<Int, Boolean>) {
        /*
        Log.d("BetaKeyFragment", "onLoadingUpdate: update=$update")
        when (update) {
            is FailureUpdate -> {
                val e: Exception? = update.e
                when (e) {
                    is IgistException -> {
                        when (e.resultMessage) {
                            ResultMessage.BAD_KEY -> {
                                showAlert(
                                    BAD_KEY_DIALOG_FRAGMENT_TAG,
                                    BAD_KEY_DIALOG_FRAGMENT_REQUEST_CODE
                                )
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
        */
    }

    // TODO NEXT !!!
    private fun onBetaKeyUpdate(update: DataUpdate<String, String>) {
        Log.d("BetaKeyFragment", "onBetaKeyUpdate: update=$update")
        when (update) {
            is PendingUpdate -> {
                binding.betaKeyEdit.isEnabled = false
                binding.submitBtn.isEnabled = false
                binding.betaKeyProgress.show()
            }
            is ResultUpdate -> {
                binding.betaKeyEdit.isEnabled = true
                binding.submitBtn.isEnabled = true
                binding.betaKeyProgress.hide()
                when (update) {
                    is SuccessUpdate -> {

                    }
                    is FailureUpdate -> {
                        val e = update.e
                        when (e) {
                            is IgistException -> {

                            }
                            else -> {
                                showAlert(
                                    PREPARING_LAUNCH_DIALOG_FRAGMENT_TAG,
                                    PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A fragment tag for the dummy book dialog fragment.
         */
        @JvmStatic
        protected val BAD_KEY_DIALOG_FRAGMENT_TAG: String =
            SelectBookFragment::class.java.name + ".BAD_KEY_DIALOG_FRAGMENT"

        // endregion Properties

    }

    // endregion Companion object

}
