/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.ui.loading.media

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES.BASE
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import javax.inject.Inject
import javax.inject.Singleton

abstract class MediaHelper(

    protected val context: Context,
    protected val fragmentManager: FragmentManager

) : LifecycleObserver, SurfaceHolder.Callback {

    // region Properties

    lateinit var absMediaFragment: AbsMediaFragment

    // endregion Properties

    // region Lifecycle methods

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.d(this::class.java.simpleName, "onCreate")
        absMediaFragment = (fragmentManager.findFragmentByTag(MEDIA_FRAGMENT_TAG) as? AbsMediaFragment)
                ?: newMediaFragment().apply {
            this@MediaHelper.fragmentManager.beginTransaction()
                .add(this, MEDIA_FRAGMENT_TAG)
                .commit()
        }
        Log.d(MediaHelper::class.java.simpleName, "onCreate: absMediaFragment=$absMediaFragment")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Log.d(this::class.java.simpleName, "onStart")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Log.d(this::class.java.simpleName, "onResume")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Log.d(this::class.java.simpleName, "onPause")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.d(this::class.java.simpleName, "onStop")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.d(this::class.java.simpleName, "onDestroy")
    }

    // endregion Lifecycle methods

    // region Implemented methods

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(this::class.java.simpleName, "surfaceCreated: holder=$holder")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(this::class.java.simpleName, "surfaceDestroyed: holder=$holder")
    }

    // endregion Implemented methods

    // region Methods

    abstract fun newMediaFragment(): AbsMediaFragment

    // endregion Methods

    // region Companion object

    companion object {

        @JvmStatic
        val MEDIA_FRAGMENT_TAG = "${MediaHelper::class.java.name}.MEDIA_FRAGMENT"

    }

    // endregion Companion object

    // region Nested inner/classes

    @Singleton
    class Factory @Inject constructor() {

        fun newInstance(context: Context, fragmentManager: FragmentManager): MediaHelper {
            return when (Build.VERSION.SDK_INT) {
                in BASE until P -> BaseMediaHelper(context, fragmentManager)
                else -> PieMediaHelper(context, fragmentManager)
            }
        }

    }

    open class AbsMediaFragment : Fragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
            Log.d(
                MediaHelper::class.java.simpleName,
                "${this::class.java.simpleName}.onCreate"
            )
        }

    }

    // endregion Nested/inner classes

}
