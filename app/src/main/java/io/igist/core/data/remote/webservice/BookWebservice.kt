/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import io.igist.core.data.remote.entity.RemoteBook
import retrofit2.Call

/**
 * A webservice that defines Book-related calls.
 */
interface BookWebservice {

    // region Methods

    /**
     * Gets a list of all books.
     */
    fun books(): Call<List<RemoteBook>>

    /**
     * Gets the book with the ID [bookId].
     */
    fun book(bookId: Long): Call<RemoteBook>

    // endregion Methods

}
