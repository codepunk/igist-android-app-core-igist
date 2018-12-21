/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import java.util.Date

/**
 * A data class representing a file required by the application.
 */
data class ContentFile(

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

}
