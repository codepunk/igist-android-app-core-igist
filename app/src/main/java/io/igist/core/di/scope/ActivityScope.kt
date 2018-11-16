/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.scope

import android.app.Activity
import javax.inject.Scope

/**
 * A [Scope] used for retaining single instances of injected dependencies throughout an [Activity]
 * lifecycle.
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope
