/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.contract

import android.os.AsyncTask
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate
import io.igist.core.domain.model.Book

/**
 * An interface that establishes the book repository contract.
 */
interface BookRepository {

    /**
     * Gets a list of all books. If [alwaysFetch] is set, the remote version will be fetched
     * regardless of whether a cached version exists.
     */
    fun getBooks(alwaysFetch: Boolean = true): LiveData<DataUpdate<List<Book>, List<Book>>>

    /**
     * Gets the book with the ID [bookId]. If [alwaysFetch] is set, the remote version will be
     * fetched regardless of whether a cached version exists.
     */
    fun getBook(bookId: Long, alwaysFetch: Boolean = true): LiveData<DataUpdate<Book, Book>>

    /**
     * A method that synchronously fetches a book. This is almost exactly the same logic as in
     * BookRepositoryImpl.BookTask but independent of any [AsyncTask], [LiveData], etc.
     */
    @WorkerThread
    fun getBookOnWorkerThread(
        bookId: Long,
        alwaysFetch: Boolean = true
    ): ResultUpdate<Void, Book>

}
