/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import androidx.room.TypeConverter
import io.igist.core.domain.model.Currency

/**
 * A Room type converter that converts between [Currency] and Int.
 */
@Suppress("UNUSED")
class CurrencyConverter {

    // region Methods

    /**
     * Converts an Int to an [Currency].
     */
    @TypeConverter
    fun toCurrency(value: Int): Currency =
        Currency.fromValue(value, Currency.UNKNOWN)

    /**
     * Converts a [Currency] to an [Int].
     */
    @TypeConverter
    fun toInt(currency: Currency): Int = currency.value

    // endregion Methods

}
