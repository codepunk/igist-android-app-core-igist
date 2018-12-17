/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.mapper.*
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.BookWebservice
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book
import retrofit2.Response
import java.util.concurrent.CancellationException

/**
 * Implementation of [BookRepository] that parses R.raw.books to get the current book list.
 */
@Suppress("UNUSED")
class BookRepositoryImpl(

    private val bookDao: BookDao,

    private val bookWebservice: BookWebservice

) : BookRepository {

    /**
     * The current [BooksTask].
     */
    private var booksTask: BooksTask? = null

    /**
     * A [LiveData] containing book list updates.
     */
    private val booksData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>> = MediatorLiveData()

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

        override fun onPreExecute() {
            super.onPreExecute()
            booksData.addSource(liveData) {
                booksData.value = it
            }
        }

        override fun doInBackground(vararg params: Void?): ResultUpdate<List<Book>, List<Book>> {
            // Retrieve any cached Books
            val localBooks = bookDao.retrieveAll()
            var books: List<Book> = localBooks.toBooks()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(books, CancellationException())

            // Fetch the latest book list
            if (books.isEmpty() || alwaysFetch) {
                // If we have a cached book list, publish it
                if (!books.isEmpty()) publishProgress(books)

                val update: ResultUpdate<Void, Response<List<RemoteBook>>> =
                    bookWebservice.books().toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(books, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(books, update.e, data)
                }

                update.result?.body()?.apply {
                    // Convert & insert remote Api into the local database
                    bookDao.deleteAll()
                    bookDao.insertAll(this.toLocalBooks())

                    // Re-retrieve the newly-inserted Api from the local database
                    books = bookDao.retrieveAll().toBooks()
                }
            }

            return SuccessUpdate(books)
        }

        override fun onPostExecute(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onPostExecute(result)
            booksData.removeSource(liveData)
        }

        override fun onCancelled(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onCancelled(result)
            booksData.removeSource(liveData)
        }

    }

    /**
     * A [DataTaskinator] that returns a [Book].
     */
    private class BookTask(

        private val bookDao: BookDao,

        private val bookWebservice: BookWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Long, Book, Book>() {

        override fun doInBackground(vararg params: Long?): ResultUpdate<Book, Book> {
            val bookId: Long = params.getOrNull(0)
                ?: throw IllegalArgumentException("No book ID passed to BookTask")

            // Retrieve any cached Book
            val localBook = bookDao.retrieve(bookId)
            var book: Book? = localBook.toBookOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(book, CancellationException())

            // Fetch the latest book metadata
            if (book == null || alwaysFetch) {
                // If we have a cached book, publish it
                if (book != null) publishProgress(book)

                val update: ResultUpdate<Void, Response<RemoteBook>> =
                    bookWebservice.book(bookId).toResultUpdate()

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(book, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(book, update.e, data)
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
        }

    }

    // endregion Nested/inner classes

}
