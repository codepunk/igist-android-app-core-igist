/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import io.igist.core.domain.model.FileType
import java.util.Date

/**
 * Remote implementation of a data class representing a file required by the application.
 */
data class RemoteContentFile(

    /**
     * The name of the file.
     */
    val filename: String,

    /**
     * The modification date of the file.
     */
    val date: Date

) {

    /**
     * The [FileType] of this content file.
     */
    val fileType: FileType = FileType.fromFilename(filename)

    /*
    /**
     * A nullable backing property for [fileType] that ensures one-time lookup.
     */
    private var _fileType: FileType? = null

    /**
     * A public, non-nullable wrapper for [_fileType] that ensures one-time lookup.
     */
    val fileType: FileType
        get() = _fileType ?: FileType.fromFilename(filename).apply { _fileType = this }
    */

}

