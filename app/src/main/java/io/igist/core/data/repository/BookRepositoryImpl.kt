/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.os.AsyncTask
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.data.remote.webservice.BookWebservice
import io.igist.core.data.resolver.BookResolver
import io.igist.core.data.resolver.BooksResolver
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book

/**
 * Implementation of [BookRepository] that parses R.raw.books to get the current book list.
 */
class BookRepositoryImpl(

    private val bookDao: BookDao,

    private val bookWebservice: BookWebservice

) : BookRepository {

    // region Properties

    /**
     * The current [BooksTask].
     */
    private var booksTask: BooksTask? = null

    /**
     * A [LiveData] containing book list updates.
     */
    private val booksData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>> = MediatorLiveData()

    // endregion Properties

    // region Implemented methods

    /**
     * Returns a [LiveData] containing book list updates.
     */
    override fun getBooks(alwaysFetch: Boolean): LiveData<DataUpdate<List<Book>, List<Book>>> {
        booksTask?.cancel(true)

        BooksTask(booksData, bookDao, bookWebservice, alwaysFetch).apply {
            booksTask = this
            executeOnExecutorAsLiveData()
        }

        return booksData
    }

    /**
     * Returns a [LiveData] containing [Book] updates.
     */
    override fun getBook(bookId: Long, alwaysFetch: Boolean): LiveData<DataUpdate<Book, Book>> =
        BookTask(bookDao, bookWebservice, alwaysFetch).executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            bookId
        )

    /**
     * A method that synchronously fetches a book. This is almost exactly the same logic as in
     * [BookTask] but independent of any [AsyncTask], [LiveData], etc.
     * TODO Maybe get rid of this
     */
    @WorkerThread
    override fun getBookOnWorkerThread(

        bookId: Long,

        alwaysFetch: Boolean

    ): ResultUpdate<Void, Book> {

        val resolver = BookResolver(bookDao, bookWebservice).shouldAlwaysFetch(alwaysFetch)

        return try {
            SuccessUpdate(resolver.get(bookId))
        } catch (e: Exception) {
            FailureUpdate(resolver.book, e)
        }

        /*
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            throw IllegalStateException(
                "Cannot invoke getBookOnWorkerThread from the main thread"
            )
        }

        // Retrieve any cached Book
        val localBook = bookDao.retrieve(bookId)
        var book: Book? = localBook.toBookOrNull()

        // Fetch the latest book metadata
        if (book == null || shouldAlwaysFetch) {
            val update: ResultUpdate<Void, Response<RemoteBook>> =
                bookWebservice.book(bookId).toResultUpdate()

            // Check if fetch failed and we have no cached book
            if (update is FailureUpdate && book == null) {
                return FailureUpdate(book, update.e)
            }

            update.result?.body()?.apply {
                // Convert & insert remote Api into the local database
                bookDao.upsert(this.toLocalBook())

                // Re-retrieve the newly-inserted Api from the local database
                bookDao.retrieve(bookId)?.let {
                    book = it.toBook()
                }
            }
        }

        return SuccessUpdate(book)
        */

    }

    // endregion Implemented methods

    // region Nested/inner classes

    /**
     * A [DataTaskinator] that retrieves a list of [Book]s.
     */
    private class BooksTask(

        private val booksData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>>,

        private val bookDao: BookDao,

        private val bookWebservice: BookWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Void, List<Book>, List<Book>>() {

        // region Inherited methods

        override fun onPreExecute() {
            super.onPreExecute()
            booksData.addSource(liveData) {
                booksData.value = it
            }
        }

        override fun doInBackground(vararg params: Void?): ResultUpdate<List<Book>, List<Book>> {
            val resolver = object : BooksResolver(bookDao, bookWebservice) {
                override val isCancelled: Boolean
                    get() = this@BooksTask.isCancelled

                override fun isValid(cached: List<Book>): Boolean = cached.isNotEmpty()

                override fun fetchRemote(params: Array<out Void?>): List<RemoteBook> {
                    if (!books.isNullOrEmpty()) {
                        publishProgress(books)
                    }
                    return super.fetchRemote(params)
                }
            }.shouldAlwaysFetch(alwaysFetch)

            return try {
                SuccessUpdate(resolver.get(*params))
            } catch (e: Exception) {
                FailureUpdate(resolver.books, e, data)
            }
        }

        override fun onPostExecute(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onPostExecute(result)
            booksData.removeSource(liveData)
        }

        override fun onCancelled(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onCancelled(result)
            booksData.removeSource(liveData)
        }

        // endregion Inherited methods

    }

    /**
     * A [DataTaskinator] that returns a [Book].
     */
    private class BookTask(

        private val bookDao: BookDao,

        private val bookWebservice: BookWebservice,

        private val alwaysFetch: Boolean = false

    ) : DataTaskinator<Long, Book, Book>() {

        // region Inherited methods

        override fun doInBackground(vararg params: Long?): ResultUpdate<Book, Book> {

            val resolver = object : BookResolver(bookDao, bookWebservice) {
                override val isCancelled: Boolean
                    get() = this@BookTask.isCancelled

                override fun fetchRemote(params: Array<out Long?>): RemoteBook? {
                    book?.run { publishProgress(this) }
                    return super.fetchRemote(params)
                }
            }.shouldAlwaysFetch(alwaysFetch)

            return try {
                SuccessUpdate(resolver.get(*params))
            } catch (e: Exception) {
                FailureUpdate(resolver.book, e, data)
            }
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
