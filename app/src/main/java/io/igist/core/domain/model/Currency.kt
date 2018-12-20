/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import android.util.SparseArray
import com.squareup.moshi.Json

/**
 * An enum class representing a type of currency.
 */
enum class Currency(

    /**
     * The numeric value associated with this [Currency].
     */
    val value: Int,

    /**
     * Whether this currency is "soft" or "hard".
     */
    val isSoft: Boolean

) {

    // region Values

    /**
     * A Currency value corresponding to an unknown currency.
     */
    @field:Json(name = "-1")
    UNKNOWN(-1, true),

    /**
     * A Currency value corresponding to "hard" currency, i.e. "Zurcon".
     */
    @field:Json(name = "0")
    ZURCON(0, false),

    /**
     * A Currency value corresponding to "soft" currency, i.e. "coins".
     */
    @field:Json(name = "1")
    COIN(1, true);

    // endregion Values

    // region Companion object

    companion object {

        // region Properties

        /**
         * An array of values for quick searching.
         */
        private val lookupArray: SparseArray<Currency> by lazy {
            SparseArray<Currency>(Currency.values().size).apply {
                for (value in Currency.values()) {
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
        fun fromValue(value: Int): Currency? = lookupArray[value]

        /**
         * Returns the [Currency] associated with the supplied [value], or [defaultValue] if
         * no such Currency exists.
         */
        fun fromValue(value: Int, defaultValue: Currency): Currency =
            lookupArray.get(value, defaultValue)

        // endregion Methods

    }

    // endregion Companion object
}
