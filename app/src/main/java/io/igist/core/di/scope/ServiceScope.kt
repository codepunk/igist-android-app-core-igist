/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.scope

import android.app.Service
import javax.inject.Scope

/**
 * A [Scope] used for retaining single instances of injected dependencies throughout a [Service]
 * lifecycle.
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceScope
