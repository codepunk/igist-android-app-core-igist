/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import com.squareup.moshi.Json

enum class StoreItemType(

    /**
     * The string value associated with this [StoreItemType].
     */
    val value: String

) {

    // region Values

    @field:Json(name = "unknown")
    UNKNOWN("unknown"),

    @field:Json(name = "card")
    CARD("card"),

    @field:Json(name = "portrait")
    PORTRAIT("portrait"),

    @field:Json(name = "sticker")
    STICKER("sticker");

    // endregion Values

    // region Companion object

    companion object {

        // region Properties

        /**
         * An array of values for quick searching.
         */
        private val lookupMap: HashMap<String, StoreItemType> by lazy {
            HashMap<String, StoreItemType>(StoreItemType.values().size).apply {
                for (value in StoreItemType.values()) {
                    put(value.value, value)
                }
            }
        }

        // endregion Properties

        // region Methods

        /**
         * Returns the [Currency] associated with the supplied [value], or `null` if no such
         * Currency exists.
         */
        @Suppress("UNUSED")
        fun fromValue(value: String): StoreItemType? = lookupMap[value]

        /**
         * Returns the [Currency] associated with the supplied [value], or [defaultValue] if
         * no such Currency exists.
         */
        fun fromValue(value: String, defaultValue: StoreItemType): StoreItemType =
            lookupMap[value] ?: defaultValue

        // endregion Methods

    }

    // endregion Companion object

}
