/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.Companion.RESULT_CANCELED
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.Companion.RESULT_NEGATIVE
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.Companion.RESULT_POSITIVE
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.OnBuildAlertDialogListener
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.codepunk.punkubator.ui.media.MediaFragment
import com.codepunk.punkubator.widget.TextureViewPanner
import io.igist.core.BuildConfig.*
import io.igist.core.R
import io.igist.core.databinding.FragmentLoadingBinding
import io.igist.core.domain.exception.IgistException
import io.igist.core.domain.session.AppSessionManager
import java.io.IOException
import javax.inject.Inject

// region Constants

/**
 * The maximum pool size for creating and storing [MediaPlayer] instances across configuration
 * changes.
 */
private const val MEDIA_FRAGMENT_MAX_POOL_SIZE = 2

/**
 * The tag to use to identify the [MediaFragment].
 */
private const val MEDIA_FRAGMENT_TAG = "MEDIA_FRAGMENT"

/**
 * The tag used to identify the [MediaPlayer] for playing the "splashy" raw mp4 resource.
 */
private const val SPLASHY_PLAYER = "SPLASHY_PLAYER"

/**
 * The request code for the "beta key required" dialog fragment.
 */
const val BETA_KEY_REQUIRED_DIALOG_FRAGMENT_REQUEST_CODE: Int = 2

// endregion Constants

/**
 * A [Fragment] that loads application data.
 */
class LoadingFragment :
    AbsLoadingFragment(),
    TextureView.SurfaceTextureListener {

    // region Properties

    /**
     * The [AppSessionManager] holding application-level session data.
     */
    @Suppress("UNUSED")
    @Inject
    lateinit var appSessionManager: AppSessionManager

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentLoadingBinding

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

    /**
     * The default visibility for [FragmentLoadingBinding.progressDescriptionTxt].
     */
    // TODO Set this based on a developer options setting
    private var defaultProgressDescriptionVisibility: Int = View.VISIBLE

    /**
     * A flag that controls whether ResultUpdate should be handled.
     */
    private var resultHandled: Boolean = true

    /**
     * A flag that indicates whether we have requested a beta key from the user so we can
     * react to updates accordingly.
     */
    private var betaKeyPageShown: Boolean = false

    // endregion Properties

    // region Lifecycle methods

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

        betaKeyPageShown = savedInstanceState?.getBoolean(KEY_LAUNCHED_BETA_KEY_PAGE) ?: false
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
        defaultProgressDescriptionVisibility = when (DEBUG) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }

    /**
     * If we arrive in this method and we've requested a beta key from the user, then we can
     * assume that we've arrived back with a result, so we can attempt to process that result.
     */
    override fun onStart() {
        super.onStart()

        // Test for whether we are awaiting the result of the beta key page
        if (betaKeyPageShown) {
            loadingViewModel.loadingUpdate.value?.run {
                resultHandled = false
                onLoadingUpdate(this)
            }
        }
    }

    /**
     * Releases the media player surface.
     */
    override fun onStop() {
        super.onStop()

        // Release the surface. This will be re-created in onSurfaceTextureAvailable when the
        // fragment starts up again.
        mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null)
        surface?.release()
        surface = null
    }

    /**
     * Saves instance state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_LAUNCHED_BETA_KEY_PAGE, betaKeyPageShown)
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

    // region Inherited methods

    /**
     * Reacts to loading LiveData updates.
     */
    override fun onLoadingUpdate(update: DataUpdate<Int, Int>) {
        when (update) {
            is ProgressUpdate -> {
                binding.progressDescriptionTxt.visibility = defaultProgressDescriptionVisibility
                binding.progressDescriptionTxt.text = update.data?.getString(KEY_DESCRIPTION)

                val progress: Int = update.progress.getOrElse(0) { 0 } ?: 0
                val max: Int = update.progress.getOrElse(1) { 0 } ?: 0
                binding.loadingProgress.visibility = View.VISIBLE
                binding.loadingProgress.progress = progress
                binding.loadingProgress.max = max
                binding.loadingProgress.isIndeterminate = (max == 0)

                resultHandled = false
            }
            is ResultUpdate -> {
                if (!resultHandled) {
                    resultHandled = true
                    when (update) {
                        is SuccessUpdate -> {
                            Log.d("LoadingFragment", "Success")
                        }
                        is FailureUpdate -> {
                            when (update.e) {
                                is IgistException -> {
                                    if (betaKeyPageShown) {
                                        betaKeyPageShown = false
                                        showAlert(
                                            BETA_KEY_REQUIRED_DIALOG_FRAGMENT_TAG,
                                            BETA_KEY_REQUIRED_DIALOG_FRAGMENT_REQUEST_CODE
                                        )
                                    } else {
                                        showBetaKeyPage()
                                    }
                                }
                                is IOException -> showAlert(
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

    /**
     * Processes dialog fragment result(s).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PREPARING_LAUNCH_DIALOG_FRAGMENT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_POSITIVE -> loadingViewModel.loadBook()
                    RESULT_NEGATIVE, RESULT_CANCELED -> requireActivity().finish()
                }
            }
            BETA_KEY_REQUIRED_DIALOG_FRAGMENT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_POSITIVE -> showBetaKeyPage()
                    RESULT_NEGATIVE, RESULT_CANCELED -> requireActivity().finish()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Implementation of [OnBuildAlertDialogListener]. Builds the appropriate alert dialog.
     */
    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        when (fragment.targetRequestCode) {
            BETA_KEY_REQUIRED_DIALOG_FRAGMENT_REQUEST_CODE -> {
                builder
                    .setTitle(R.string.loading_dialog_beta_key_required_title)
                    .setMessage(R.string.loading_dialog_beta_key_required_message)
                    .setPositiveButton(R.string.app_retry, fragment)
                    .setNegativeButton(R.string.app_quit, fragment)
            }
            else -> super.onBuildAlertDialog(fragment, builder)
        }
    }

    // endregion Inherited methods

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
        val mediaPlayer: MediaPlayer = mediaFragment.mediaPlayers.obtain(SPLASHY_PLAYER) {
            MediaPlayer.create(requireContext(), R.raw.splashy).apply {
                isLooping = true
            }
        }

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
        /* TODO Causes IllegalStateException */
        if (activity?.isFinishing == false) {
            mediaFragment.mediaPlayers[SPLASHY_PLAYER]?.setSurface(null)
            surface?.release()
            surface = null
        }
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

    private fun showBetaKeyPage() {
        betaKeyPageShown = true
        Navigation.findNavController(
            requireActivity(),
            R.id.loading_nav_fragment
        ).navigate(R.id.action_loading_to_beta_key)
    }

    // region Companion object

    companion object {

        // region Properties

        /**
         * A fragment tag for the dummy book dialog fragment.
         */
        @JvmStatic
        private val BETA_KEY_REQUIRED_DIALOG_FRAGMENT_TAG: String =
            LoadingFragment::class.java.name + ".BETA_KEY_REQUIRED_DIALOG_FRAGMENT"

        // endregion Properties

    }

    // endregion Companion object

}
