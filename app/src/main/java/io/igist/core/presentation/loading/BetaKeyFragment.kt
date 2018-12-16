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
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.BuildConfig.KEY_RESULT_MESSAGE
import io.igist.core.R
import io.igist.core.databinding.FragmentBetaKeyBinding
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.model.ResultMessage
import io.igist.core.presentation.base.BaseActivity

// region Constants

/**
 * The request code for a bad beta key dialog fragment.
 */
private const val RESULT_MESSAGE_DIALOG_FRAGMENT_REQUEST_CODE: Int = 2

// endregion Constants

class BetaKeyFragment :
    AbsLoadingFragment(),
    BaseActivity.OnBackPressedListener,
    View.OnClickListener,
    AlertDialogFragment.OnBuildAlertDialogListener {

    // region Properties

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentBetaKeyBinding

    private val baseActivity: BaseActivity?
        get() = activity as? BaseActivity

    /**
     * A flag that controls whether ResultUpdate should be handled.
     */
    private var handleResultUpdate: Boolean = false

    /**
     * The most recent Igist remote message.
     */
    private var resultMessage: ResultMessage? = null

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
     * Restores instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultMessage = savedInstanceState?.getParcelable(KEY_RESULT_MESSAGE)
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

    override fun onStart() {
        super.onStart()
        (activity as? BaseActivity)?.registerOnBackPressedListener(this)
    }

    override fun onStop() {
        super.onStop()
        (activity as? BaseActivity)?.unregisterOnBackPressedListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_RESULT_MESSAGE, resultMessage)
    }

    // endregion Lifecycle methods

    // region Inherited methods

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE -> {

            }
            RESULT_MESSAGE_DIALOG_FRAGMENT_REQUEST_CODE -> {
                resultMessage = null
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Responds to the user pressing the back button.
     */
    override fun onBackPressed(activity: BaseActivity): Boolean {
        return when (loadingViewModel.liveBetaKey.value) {
            is SuccessUpdate -> false
            else -> {
                activity.finish()
                true
            }
        }
    }

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
            RESULT_MESSAGE_DIALOG_FRAGMENT_REQUEST_CODE -> {
                builder
                    .setTitle(R.string.beta_key_dialog_bad_key_title)
                    .setPositiveButton(android.R.string.ok, fragment)
                when (resultMessage) {
                    null -> builder.setMessage(R.string.beta_key_dialog_bad_key_message)
                    else -> builder.setMessage(resultMessage?.getString(requireContext()))
                }
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
    // Also, on "back button"/cancel, should just exit the app
    override fun onLoadingUpdate(update: DataUpdate<Int, Boolean>) {

    }

    // TODO NEXT !!!
    private fun onBetaKeyUpdate(update: DataUpdate<String, String>?) {
        Log.d("BetaKeyFragment", "onBetaKeyUpdate: update=$update")
        when (update) {
            is PendingUpdate -> {
                /* TODO Rethink this Pending/Progress/etc.
                binding.betaKeyEdit.isEnabled = false
                binding.submitBtn.isEnabled = false
                binding.betaKeyProgress.show()
                */
            }
            is ProgressUpdate -> {
                binding.betaKeyEdit.isEnabled = false
                binding.submitBtn.isEnabled = false
                binding.betaKeyProgress.show()
                handleResultUpdate = true
            }
            is ResultUpdate -> {
                if (handleResultUpdate) {
                    handleResultUpdate = false
                    binding.betaKeyEdit.isEnabled = true
                    binding.submitBtn.isEnabled = true
                    binding.betaKeyProgress.hide()
                    when {
                        update is SuccessUpdate -> {

                        }
                    }



                    when (update) {
                        is SuccessUpdate -> {

                        }
                        is FailureUpdate -> {
                            val e = update.e
                            when (e) {
                                is IgistException -> {
                                    when (e.resultMessage) {
                                        ResultMessage.BETA_KEY_REQUIRED -> {
                                            // No action
                                            return
                                        }
                                    }
                                    resultMessage = e.resultMessage
                                    showAlert(
                                        RESULT_MESSAGE_DIALOG_FRAGMENT_TAG,
                                        RESULT_MESSAGE_DIALOG_FRAGMENT_REQUEST_CODE
                                    )
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
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A fragment tag for the dummy book dialog fragment.
         */
        @JvmStatic
        protected val RESULT_MESSAGE_DIALOG_FRAGMENT_TAG: String =
            SelectBookFragment::class.java.name + ".RESULT_MESSAGE_DIALOG_FRAGMENT"

        // endregion Properties

    }

    // endregion Companion object

}
