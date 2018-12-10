/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

/**
 * A data class representing a book.
 */
data class Book(

    /**
     * The book ID.
     */
    val id: Long,

    /**
     * The book title.
     */
    val title: String,

    /**
     * The book description.
     */
    val description: String,

    /**
     * An image name associated with this book.
     */
    val imageName: String,

    /**
     * The book API version. This specifies the "mode" (i.e. beta key required), survey link
     * presented at the end of the book, etc.
     */
    val apiVersion: Int,

    /**
     * The book app version. This specifies the version of the actual content (i.e. images,
     * store data, etc.).
     */
    val appVersion: Int

)
