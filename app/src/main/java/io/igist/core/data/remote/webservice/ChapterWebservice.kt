package io.igist.core.data.remote.webservice

import io.igist.core.data.remote.entity.RemoteChapter
import retrofit2.Call

/**
 * A webservice that defines chapter-related calls.
 */
interface ChapterWebservice {

    // region Methods

    /**
     * Gets a list of all chapters for a given [bookId].
     */
    fun chapters(bookId: Long): Call<List<RemoteChapter>>

    /**
     * Gets the chapter with the ID [bookId] and chapter number [chapterNumber].
     */
    fun chapter(bookId: Long, chapterNumber: Int): Call<RemoteChapter>

    // endregion Methods

}
