/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import android.util.SparseArray
import com.squareup.moshi.Json

/**
 * An enum class that describes the "mode" (i.e. behavior) associated with the current API version.
 */
enum class IgistMode(val value: Int) {

    /**
     * A mode that indicates no special behavior.
     */
    @field:Json(name = "0")
    NONE(0),

    /**
     * A mode that indicates that a beta key is required to use the app.
     */
    @field:Json(name = "1")
    SHOW_BETA_PAGE(1);

    companion object {

        private val lookup: SparseArray<IgistMode> by lazy {
            SparseArray<IgistMode>(values().size).apply {
                for (value in values()) {
                    put(value.value, value)
                }
            }
        }

        fun fromValue(value: Int): IgistMode? = lookup[value]

        fun fromValue(value: Int, defaultValue: IgistMode): IgistMode =
            lookup.get(value, defaultValue)

    }

}
