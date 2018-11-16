/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.qualifier

import android.app.Application
import android.content.Context
import javax.inject.Qualifier

/**
 * A [Qualifier] that specifies an [Application]-level [Context].
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContext
