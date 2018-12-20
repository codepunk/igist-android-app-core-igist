/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import androidx.room.TypeConverter
import io.igist.core.domain.model.StoreItemType

/**
 * A Room type converter that converts between [StoreItemType] and Int.
 */
@Suppress("UNUSED")
class StoreItemTypeConverter {

    // region Methods

    /**
     * Converts an Int to an [StoreItemType].
     */
    @TypeConverter
    fun toStoreItemType(value: String): StoreItemType =
        StoreItemType.fromValue(value, StoreItemType.UNKNOWN)

    /**
     * Converts a [StoreItemType] to a [String].
     */
    @TypeConverter
    fun toString(type: StoreItemType): String = type.value

    // endregion Methods

}
