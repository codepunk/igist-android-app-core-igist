/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.igist.core.R
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.mapper.toBooks
import io.igist.core.data.mapper.toLocalBooks
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

class BookRepositoryImpl(

    @ApplicationContext
    private val context: Context,

    private val sharedPreferences: SharedPreferences,

    private val bookDao: BookDao,

    private val moshi: Moshi

) : BookRepository {

    private var bookTask: BookTask? = null

    val bookData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>> = MediatorLiveData()

    override fun getBooks(): LiveData<DataUpdate<List<Book>, List<Book>>> {

        bookTask?.cancel(true)

        BookTask(context, bookData, bookDao, moshi).apply {
            bookTask = this
            executeOnExecutorAsLiveData()
        }

        return bookData
    }

    private class BookTask(

        context: Context,

        private val bookData: MediatorLiveData<DataUpdate<List<Book>, List<Book>>>,

        private val bookDao: BookDao,

        private val moshi: Moshi

    ) : DataTaskinator<Void, List<Book>, List<Book>>() {

        private val weakContext: WeakReference<Context> = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            bookData.addSource(liveData) {
                bookData.value = it
            }
        }

        override fun doInBackground(vararg params: Void?): ResultUpdate<List<Book>, List<Book>> {
            // Retrieve any cached Api
            val localBooks = bookDao.retrieveAll()
            var books: List<Book> = localBooks.toBooks()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(books, CancellationException())

            // If we have a cached book list, publish it
            if (!books.isEmpty()) publishProgress(books)

            // Fetch the latest book list. Currently this is baked into the app in a JSON file
            // in the "raw" resource folder, but might one day be returned from the server.
            val context = weakContext.get()
                ?: throw IllegalStateException("No context available")
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
            if (remoteBooks.isEmpty() && books.isEmpty()) {
                return FailureUpdate(books, java.lang.IllegalStateException("No books were found"))
            }

            // Check if cancelled
            if (isCancelled) return FailureUpdate(books, CancellationException())

            // Convert & insert remote books into the local database
            bookDao.insertAll(remoteBooks.toLocalBooks())

            // Re-retrieve the newly-inserted Api from the local database
            books = bookDao.retrieveAll().toBooks()

            return SuccessUpdate(books)
        }

        override fun onPostExecute(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onPostExecute(result)
            bookData.removeSource(liveData)
        }

        override fun onCancelled(result: ResultUpdate<List<Book>, List<Book>>?) {
            super.onCancelled(result)
            bookData.removeSource(liveData)
        }

    }

}
