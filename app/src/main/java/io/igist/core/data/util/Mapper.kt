/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.util

abstract class Mapper<in Src, Dst> {

    abstract fun map(source: Src): Dst

}
