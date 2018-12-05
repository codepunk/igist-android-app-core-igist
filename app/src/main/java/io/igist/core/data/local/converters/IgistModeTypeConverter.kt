/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import androidx.room.TypeConverter
import io.igist.core.domain.model.IgistMode

class IgistModeTypeConverter {

    // region Methods

    /**
     * Converts an Int to an [IgistMode].
     */
    @TypeConverter
    fun toIgistMode(value: Int): IgistMode = IgistMode.fromValue(value, IgistMode.NONE)

    /**
     * Converts an [IgistMode] to an [Int].
     */
    @TypeConverter
    fun toInt(mode: IgistMode): Int = mode.value

    // endregion Methods

}
