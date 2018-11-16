/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.qualifier

import android.app.Activity
import android.content.Context
import javax.inject.Qualifier

/**
 * A [Qualifier] that specifies an [Activity]-level [Context].
 */
@Suppress("UNUSED")
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityContext
