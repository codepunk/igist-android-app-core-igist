/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.converter

import com.squareup.moshi.Json
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Field
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * A [Converter.Factory] for leveraging Moshi @[Json] annotations to use to convert enum values
 * in Retrofit calls.
 *
 * See also: https://stackoverflow.com/questions/35793344/how-to-pass-custom-enum-in-query-via-retrofit
 */
class MoshiEnumConverterFactory @Inject constructor() : Converter.Factory() {

    // region Inherited methods

    /**
     * Looks for a @[Json] annotation on an enum value and uses that as the converted value if it
     * exists. If no such annotation is present, it returns the value's default [toString] return
     * value.
     */
    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return when {
            type is Class<*> && type.isEnum -> Converter<Any?, String> { value ->
                val enum: Enum<*> = value as Enum<*>
                val field: Field = enum::class.java.getField(enum.name)
                field.getAnnotation(Json::class.java)?.name ?: enum.toString()
            }
            else -> null
        }
    }

    // endregion Inherited methods

}
