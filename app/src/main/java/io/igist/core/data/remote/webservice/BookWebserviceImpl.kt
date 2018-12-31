/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.webservice

import android.content.Context
import android.content.res.Resources
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.igist.core.R
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.di.qualifier.ApplicationContext
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type

/**
 * An implementation of [BookWebservice] that retrieves books from a local raw resource while
 * mimicking the functionality of a Retrofit [Call].
 */
class BookWebserviceImpl(

    /**
     * The application [Context].
     */
    @ApplicationContext
    private val context: Context,

    /**
     * A [Moshi] instance for decoding JSON files.
     */
    private val moshi: Moshi

) : BookWebservice {

    // region Implemented methods

    /**
     * Gets a list of all books.
     */
    override fun books(): Call<List<RemoteBook>> = object : CallImpl<List<RemoteBook>>() {

        // region Inherited methods

        override fun execute(): Response<List<RemoteBook>>? = Response.success(decodeBooks())

        // endregion Inherited methods

    }

    /**
     * Gets the book with the ID [bookId].
     */
    override fun book(bookId: Long): Call<RemoteBook> = object : CallImpl<RemoteBook>() {

        // region Inherited methods

        override fun execute(): Response<RemoteBook> {
            val remoteBooks = decodeBooks()
            val remoteBook = remoteBooks?.find { (id) -> id == bookId }
            return Response.success(remoteBook)
        }

        // endregion Inherited methods

    }

    // endregion Implemented methods

    // region Methods

    /**
     * Decodes the list of books in the R.raw.books JSON resource into a list of [RemoteBook].
     */
    @Throws(Resources.NotFoundException::class, IOException::class)
    private fun decodeBooks(): List<RemoteBook>? {
        val remoteBooks: List<RemoteBook>?
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
            }
        } catch (e: Resources.NotFoundException) {
            throw e
        } catch (e: IOException) {
            throw e
        } finally {
            try {
                booksInputStream?.close()
            } catch (e: IOException) {
                // No op
            }
        }
        return remoteBooks
    }

    // endregion Methods

}
