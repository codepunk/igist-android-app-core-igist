/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import androidx.room.TypeConverter
import io.igist.core.domain.model.FileCategory

/**
 * A Room type converter that converts between [FileCategory] and Int.
 */
@Suppress("UNUSED")
class FileCategoryTypeConverter {

    // region Methods

    /**
     * Converts an Int to a [FileCategory].
     */
    @TypeConverter
    fun toFileCategory(value: Int): FileCategory =
        FileCategory.fromValue(value, FileCategory.UNKNOWN)

    /**
     * Converts a [FileCategory] to an [Int].
     */
    @TypeConverter
    fun toInt(mode: FileCategory): Int = mode.value

    // endregion Methods

}
