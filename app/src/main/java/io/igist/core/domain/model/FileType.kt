/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import java.io.File

/**
 * An enum class indicating the type of file.
 */
enum class FileType(

    /**
     * An array of extensions associated with the [FileType].
     */
    vararg val extensions: String

) {

    // region Values

    /**
     * A FileType indicating a GIF image file.
     */
    GIF("gif"),

    /**
     * A FileType indicating a JPEG image file.
     */
    JPG("jpg", "jpeg"),

    /**
     * A FileType indicating an MP4 video file.
     */
    MP4("mp4"),

    /**
     * A FileType indicating a PNG file.
     */
    PNG("png"),

    /**
     * A FileType indicating a file with an unknown file type.
     */
    UNKNOWN();

    // endregion Values

    // region Companion object

    companion object {

        // region Properties

        /**
         * A lookup map associating extensions with [FileType]s.
         */
        private val lookupMap: Map<String, FileType> by lazy {
            HashMap<String, FileType>().apply {
                values().forEach { fileType ->
                    fileType.extensions.forEach { extension ->
                        put(extension, fileType)
                    }
                }
            }
        }

        // endregion Properties

        // region Methods

        /**
         * A method that takes a [filename] and returns the associated [FileType], or
         * [UNKNOWN] if the file type cannot be determined.
         */
        fun fromFilename(filename: String): FileType =
            lookupMap[File(filename).extension.toLowerCase()] ?: UNKNOWN

        // endregion Methods

    }

    // endregion Companion object

}
