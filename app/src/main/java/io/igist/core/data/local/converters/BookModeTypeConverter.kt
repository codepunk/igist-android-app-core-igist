/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import androidx.room.TypeConverter
import io.igist.core.domain.model.BookMode

class BookModeTypeConverter {

    // region Methods

    /**
     * Converts an Int to an [BookMode].
     */
    @TypeConverter
    fun toIgistMode(value: Int): BookMode = BookMode.fromValue(value, BookMode.NONE)

    /**
     * Converts an [BookMode] to an [Int].
     */
    @TypeConverter
    fun toInt(mode: BookMode): Int = mode.value

    // endregion Methods

}
