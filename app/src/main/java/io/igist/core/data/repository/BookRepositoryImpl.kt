/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.igist.core.R
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.mapper.*
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.di.qualifier.ApplicationContext
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CancellationException

/**
 * Implementation of [BookRepository] that parses R.raw.books to get the current book list.
 */
@Suppress("UNUSED")
class BookRepositoryImpl(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val bookDao: BookDao,

    private val moshi: Moshi

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

        BooksTask(context, booksData, bookDao, moshi, alwaysFetch).apply {
            booksTask = this
            executeOnExecutorAsLiveData()
        }

        return booksData
    }

    /**
     * Returns a [LiveData] containing [Book] updates.
     */
    override fun getBook(bookId: Long, alwaysFetch: Boolean): LiveData<DataUpdate<Book, Book>> =
        BookTask(context, bookDao, moshi, alwaysFetch).executeOnExecutorAsLiveData(
            AsyncTask.THREAD_POOL_EXECUTOR,
            bookId
        )

    // region Companion object

    companion object {

        // region Methods

        /**
         * A temporary convenience method that returns a [List] of all [RemoteBook]s from the
         * R.raw.books resource. If future iterations of this app allow for multiple book
         * selection, this information should be fetched from the network instead.
         */
        private fun tempFetchAllRemoteBooks(
            context: Context,
            moshi: Moshi
        ): List<RemoteBook> {
            val remoteBooks: List<RemoteBook>
            var booksInputStream: InputStream? = null
            try {
                booksInputStream = context.resources.openRawResource(R.raw.books).apply {
                    val buffer = ByteArray(available())
                    read(buffer)
                    val json = String(buffer, Charsets.UTF_8)
                    val type: Type =
                        Types.newParameterizedType(List::class.java, RemoteBook::class.java)
                    val adapter: JsonAdapter<List<RemoteBook>> = moshi.adapter(type)
                    remoteBooks = adapter.fromJson(json)
                            ?: Collections.unmodifiableList(Collections.emptyList())
                }
            } finally {
                try {
                    booksInputStream?.close()
                } catch (e: IOException) {
                    // No op
                }
            }
            return remoteBooks
        }

        /**
         * A temporary convenience method that returns a [RemoteBook] from the R.raw.books
         * resource with the supplied [bookId]. If future iterations of this app allow for
         * multiple book selection, this information should be fetched from the network instead.
         */
        private fun tempFetchRemoteBook(
            context: Context,
            moshi: Moshi,
            bookId: Long
        ): RemoteBook? {
            val remoteBooks: List<RemoteBook> = tempFetchAllRemoteBooks(context, moshi)
            return remoteBooks.find { (id) -> id == bookId }
        }

        // endregion Methods

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * A [DataTaskinator] that retrieves a list of [Book]s.
     */
    private class BooksTask(

        context: Context,

        private val booksData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>>,

        private val bookDao: BookDao,

        private val moshi: Moshi,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Void, List<Book>, List<Book>>() {

        private val weakContext: WeakReference<Context> = WeakReference(context)

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

            // If we have a cached book list, publish it
            if (!books.isEmpty()) publishProgress(books)

            // Fetch the latest book list. Currently this is baked into the app in a JSON file
            // in the "raw" resource folder, but might one day be returned from the server.
            if (books.isEmpty() || alwaysFetch) {
                val context = weakContext.get()
                    ?: throw IllegalStateException("No context available")
                val remoteBooks: List<RemoteBook> = tempFetchAllRemoteBooks(context, moshi)
                if (remoteBooks.isEmpty() && books.isEmpty()) {
                    return FailureUpdate(
                        books,
                        java.lang.IllegalStateException("No books were found")
                    )
                }

                // Check if cancelled
                if (isCancelled) return FailureUpdate(books, CancellationException())

                // Convert & insert remote books into the local database
                bookDao.deleteAll()
                bookDao.insertAll(remoteBooks.toLocalBooks())

                // Re-retrieve the newly-inserted Api from the local database
                books = bookDao.retrieveAll().toBooks()
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

        context: Context,

        private val bookDao: BookDao,

        private val moshi: Moshi,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Long, Book, Book>() {

        private val weakContext: WeakReference<Context> = WeakReference(context)

        override fun doInBackground(vararg params: Long?): ResultUpdate<Book, Book> {
            val bookId: Long = params.getOrNull(0)
                ?: throw IllegalArgumentException("No book ID passed to BookTask")

            // Retrieve any cached Book
            val localBook = bookDao.retrieve(bookId)
            var book: Book? = localBook.toBookOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(book, CancellationException())

            // If we have a cached book list, publish it
            if (book != null) publishProgress(book)

            // Fetch the latest book info. Currently this is baked into the app in a JSON file
            // in the "raw" resource folder, but might one day be returned from the server.
            if (book == null || alwaysFetch) {
                val context = weakContext.get()
                    ?: throw IllegalStateException("No context available")
                val remoteBook: RemoteBook? = tempFetchRemoteBook(context, moshi, bookId)
                if (remoteBook == null && book == null) {
                    return FailureUpdate(
                        book,
                        java.lang.IllegalStateException("No book was found with book id $bookId")
                    )
                }

                // Check if cancelled
                if (isCancelled) return FailureUpdate(book, CancellationException())

                remoteBook?.run {
                    // Convert & insert remote books into the local database
                    bookDao.insert(this.toLocalBook())

                    // Re-retrieve the newly-inserted Api from the local database
                    bookDao.retrieve(bookId)?.run {
                        book = this.toBook()
                    }
                }
            }

            return SuccessUpdate(book)
        }

    }

    // endregion Nested/inner classes

}
