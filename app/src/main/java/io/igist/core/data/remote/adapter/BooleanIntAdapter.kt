/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.igist.core.data.remote.annotation.BooleanInt
import javax.inject.Inject

/**
 * A JSON adapter that converts between integers and booleans.
 */
class BooleanIntAdapter @Inject constructor() {

    // region Methods

    /**
     * Converts a boolean to a JSON integer.
     */
    @Suppress("UNUSED")
    @ToJson
    fun toJson(@BooleanInt value: Boolean): Int {
        return when (value) {
            true -> 1
            else -> 0
        }
    }

    /**
     * Converts a JSON integer to a boolean.
     */
    @Suppress("UNUSED")
    @FromJson
    @BooleanInt
    fun fromJson(value: Int): Boolean {
        return when (value) {
            0 -> false
            else -> true
        }
    }

    // endregion Methods

}
