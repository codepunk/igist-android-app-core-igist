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
enum class BookMode(

    /**
     * The numeric value associated with this [BookMode].
     */
    val value: Int

) {

    /**
     * A mode that indicates no special behavior.
     */
    @field:Json(name = "0")
    NONE(0),

    /**
     * A mode that indicates that a beta key is required to read a book.
     */
    @field:Json(name = "1")
    REQUIRE_BETA_KEY(1);

    companion object {

        /**
         * An array of values for quick searching.
         */
        private val lookupArray: SparseArray<BookMode> by lazy {
            SparseArray<BookMode>(values().size).apply {
                for (value in values()) {
                    put(value.value, value)
                }
            }
        }

        /**
         * Returns the [BookMode] associated with the supplied [value], or `null` if no such
         * BookMode exists.
         */
        @Suppress("UNUSED")
        fun fromValue(value: Int): BookMode? = lookupArray[value]

        /**
         * Returns the [BookMode] associated with the supplied [value], or [defaultValue] if
         * no such BookMode exists.
         */
        fun fromValue(value: Int, defaultValue: BookMode): BookMode =
            lookupArray.get(value, defaultValue)

    }

}
