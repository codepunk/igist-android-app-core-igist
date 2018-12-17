/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.local.converters

import android.util.Log
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [TypeConverter] that converts between [Date] and [String], using the format
 * "yyyy-MM-dd HH:mm:ss".
 */
class DateConverter {

    // region Methods

    /**
     * Converts a [String] to a [Date].
     */
    @TypeConverter
    fun toDate(string: String): Date {
        return try {
            dateFormat.parse(string)
        } catch (e: Exception) {
            // TODO This error is happening
            Log.e("DateConverter", "${e.message}; string=$string", e)
            Date()
        }
    }

    /**
     * Converts a [Date] to a [String].
     */
    @TypeConverter
    fun toString(date: Date): String = dateFormat.format(date)

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * The [SimpleDateFormat] to use when converting between [Date]s and [String]s.
         */
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        // endregion Properties

    }

    // endregion Companion object}

}
