/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import android.util.SparseArray
import com.squareup.moshi.Json

/**
 * An enum class that describes the category of content file.
 */
enum class FileCategory(

    /**
     * The numeric value associated with this [FileCategory].
     */
    val value: Int

) {

    // region Values

    /**
     * A FileCategory value corresponding to an unknown file category.
     */
    @field:Json(name = "0")
    UNKNOWN(0),

    /**
     * A FileCategory value corresponding to a chapter image.
     */
    @field:Json(name = "1")
    CHAPTER_IMAGE(1),

    /**
     * A FileCategory value corresponding to a sputnik media file.
     */
    @field:Json(name = "2")
    SPUTNIK(2),

    /**
     * A FileCategory value corresponding to a badge media file.
     */
    @field:Json(name = "3")
    BADGE(3),

    /**
     * A FileCategory value corresponding to a storefront media file.
     */
    @field:Json(name = "4")
    STOREFRONT(4);

    // endregion Values

    // region Companion object

    companion object {

        // region Properties

        /**
         * An array of values for quick searching.
         */
        private val lookupArray: SparseArray<FileCategory> by lazy {
            SparseArray<FileCategory>(values().size).apply {
                for (value in values()) {
                    put(value.value, value)
                }
            }
        }

        // endregion Properties

        // region Methods

        /**
         * Returns the [FileCategory] associated with the supplied [value], or `null` if no such
         * FileCategory exists.
         */
        @Suppress("UNUSED")
        fun fromValue(value: Int): FileCategory? = lookupArray[value]

        /**
         * Returns the [FileCategory] associated with the supplied [value], or [defaultValue] if
         * no such FileCategory exists.
         */
        fun fromValue(value: Int, defaultValue: FileCategory): FileCategory =
            lookupArray.get(value, defaultValue)

        // endregion Methods

    }

    // endregion Companion object

}
