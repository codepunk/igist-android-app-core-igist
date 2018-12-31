package io.igist.core.data.remote.webservice

import com.codepunk.doofenschmirtz.util.http.HttpStatus
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.igist.core.BuildConfig.APP_FILES_DIRECTORY
import io.igist.core.BuildConfig.BASE_URL
import io.igist.core.data.remote.entity.RemoteChapter
import io.igist.core.data.resolver.BookResolver
import io.igist.core.domain.model.Book
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Response
import xmlwise.Plist
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.net.URL

/**
 * An implementation of [ChapterWebservice] that retrieves chapters from a PLIST on the network
 * while mimicking the functionality of a Retrofit [Call].
 */
class ChapterWebserviceImpl(

    /**
     * A [BookResolver] for resolving local/remote books.
     */
    private val bookResolver: BookResolver,

    /**
     * A [Moshi] instance for decoding JSON files.
     */
    private val moshi: Moshi

) : ChapterWebservice {

    // region Properties

    var currentCall: CallImpl<List<RemoteChapter>>? = null

    // endregion Properties

    // region Implemented methods

    override fun chapters(bookId: Long): Call<List<RemoteChapter>> {
        currentCall?.cancel()
        return object : CallImpl<List<RemoteChapter>>() {
            override fun execute(): Response<List<RemoteChapter>> =
                decodeChapters(this, bookId)
        }.apply {
            currentCall = this
        }
    }

    override fun chapter(bookId: Long, chapterNumber: Int): Call<RemoteChapter> =
        object : CallImpl<RemoteChapter>() {
            override fun execute(): Response<RemoteChapter> {
                TODO("not implemented")
            }
        }

    // endregion Implemented methods

    // region Methods

    /**
     * Downloads the Plist associated with the given [bookId] and casts it to a [List] of
     * [RemoteChapter]s.
     */
    @Suppress("UNCHECKED_CAST")
    private fun decodeChapters(
        call: CallImpl<List<RemoteChapter>>,
        bookId: Long
    ): Response<List<RemoteChapter>> {
        val book: Book = try {
            bookResolver
                .shouldAlwaysFetch(false)
                .get(bookId)
        } catch (e: Exception) {
            return responseError(HttpStatus.NOT_FOUND, e.message)
        } ?: return responseError(
            HttpStatus.NOT_FOUND,
            "No book found with ID $bookId"
        )

        val url = URL("$BASE_URL/$APP_FILES_DIRECTORY/${book.plistFile}")
        var chaptersInputStream: InputStream? = null
        try {
            chaptersInputStream = url.openStream()
            val byteArray = BufferedInputStream(chaptersInputStream).readBytes()
            val xml = String(byteArray)

            // The object resulting from converting the Plist is a List<List<Map>>, so we will
            // strip off the first element of the outer list in order to create the JSON below
            // (since that's all we are interested in).
            val resultObject = Plist.objectFromXml(xml)
            val resultList = resultObject as List<List<Map<String, String>>>
            val json: String = JSONArray(resultList[0]).toString()
            val type: Type =
                Types.newParameterizedType(List::class.java, RemoteChapter::class.java)
            val adapter: JsonAdapter<List<RemoteChapter>> = moshi.adapter(type)
            val chapters = adapter.fromJson(json)

            return Response.success(chapters)
        } catch (e: java.lang.Exception) {
            return responseError(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        } finally {
            try {
                chaptersInputStream?.close()
            } catch (e: IOException) { /* No op */
            }
        }
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Methods

        /**
         * Generates an error [Response] using the supplied [status] and [xml].
         */
        private fun responseError(
            status: HttpStatus,
            content: String?
        ): Response<List<RemoteChapter>> =
            Response.error(
                status.code,
                ResponseBody.create(MediaType.parse("text/plain"), content ?: "")
            )

        // endregion Methods

    }

    // endregion Companion object

}
