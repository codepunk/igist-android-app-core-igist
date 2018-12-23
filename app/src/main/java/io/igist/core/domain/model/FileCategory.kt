/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import android.content.Context
import android.util.SparseArray
import com.squareup.moshi.Json
import io.igist.core.BuildConfig

/**
 * An enum class that describes the category of content file.
 */
enum class FileCategory(

    /**
     * The numeric value associated with this [FileCategory].
     */
    val value: Int,

    /**
     * The name of the directory in which to find this file category type.
     */
    val directory: String

) {

    // region Values

    /**
     * A FileCategory value corresponding to an unknown file category.
     */
    @field:Json(name = "0")
    UNKNOWN(0, "unknown"),

    /**
     * A FileCategory value corresponding to a chapter image.
     */
    @field:Json(name = "1")
    CHAPTER_IMAGE(1, "chapter_images"),

    /**
     * A FileCategory value corresponding to a sputnik media file.
     */
    @field:Json(name = "2")
    SPUTNIK(2, "sputniks"),

    /**
     * A FileCategory value corresponding to a badge media file.
     */
    @field:Json(name = "3")
    BADGE(3, "badges"),

    /**
     * A FileCategory value corresponding to a storefront media file.
     */
    @field:Json(name = "4")
    STOREFRONT(4, "store_front");

    // endregion Values

    // region Methods

    /**
     * Returns a local directory for files of this [FileCategory], using the given [context] and
     * [bookId].
     */
    fun getLocalDir(context: Context, bookId: Long): String =
        "${context.filesDir.path}/${BuildConfig.APP_FILES_DIRECTORY}/$bookId/$directory"

    /**
     * Returns a network directory for files of this [FileCategory]. Note that [bookId] is not
     * currently being used but we're including it here for that possibility in the future.
     */
    @Suppress("UNUSED_PARAMETER")
    fun getNetworkDir(bookId: Long): String =
        "${BuildConfig.BASE_URL}/${BuildConfig.APP_FILES_DIRECTORY}/$directory"

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * An array of values for quick searching.
         */
        private val lookupArray: SparseArray<FileCategory> by lazy {
            SparseArray<FileCategory>(values().size).apply {
                for (value in values()) {
                    put(value.value, value)
                }
            }
        }

        // endregion Properties

        // region Methods

        /**
         * Returns the [FileCategory] associated with the supplied [value], or `null` if no such
         * FileCategory exists.
         */
        @Suppress("UNUSED")
        fun fromValue(value: Int): FileCategory? = lookupArray[value]

        /**
         * Returns the [FileCategory] associated with the supplied [value], or [defaultValue] if
         * no such FileCategory exists.
         */
        fun fromValue(value: Int, defaultValue: FileCategory): FileCategory =
            lookupArray.get(value, defaultValue)

        // endregion Methods

    }

    // endregion Companion object

}
