/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A [JsonAdapter] that converts JSON date strings into [Date] instances.
 */
@Singleton
class DateJsonAdapter @Inject constructor() : JsonAdapter<Date>() {

    // region Inherited methods

    /**
     * Converts a JSON date to a [Date] instance.
     */
    override fun fromJson(reader: JsonReader): Date? {
        val source = reader.nextString()
        try {
            return DATE_FORMAT.parse(source)
        } catch (e: ParseException) {
            throw IOException(e)
        }
    }

    /**
     * Converts a [Date] to a JSON date string.
     */
    override fun toJson(writer: JsonWriter, value: Date?) {
        val valueString = DATE_FORMAT.format(value)
        writer.value(valueString)
    }

    // endregion Inherited methods

    // region Companion object

    companion object {

        // region Properties

        private val DATE_FORMAT: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)

        // endregion Properties

    }

    // endregion Companion object
}
