package io.igist.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.domain.model.Book
import io.igist.core.domain.model.Chapter

/**
 * An interface that establishes the chapter repository contract.
 */
interface ChapterRepository {

    /**
     * Gets a list of all chapters for the supplied [bookId]. If [alwaysFetch] is set, the remote
     * version will be fetched regardless of whether a cached version exists.
     */
    fun getChapters(bookId: Long, alwaysFetch: Boolean = true):
            LiveData<DataUpdate<List<Chapter>, List<Chapter>>>

    /**
     * Gets the chapter associated with the ID [bookId] and [chapterNumber]. If [alwaysFetch]
     * is set, the remote version will be fetched regardless of whether a cached version exists.
     */
    fun getChapter(bookId: Long, chapterNumber: Int, alwaysFetch: Boolean = true):
            LiveData<DataUpdate<Book, Book>>


}
