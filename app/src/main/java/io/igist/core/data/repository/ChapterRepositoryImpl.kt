package io.igist.core.data.repository

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.*
import io.igist.core.data.local.dao.ChapterDao
import io.igist.core.data.local.entity.LocalChapter
import io.igist.core.data.mapper.toChaptersOrNull
import io.igist.core.data.mapper.toLocalChapters
import io.igist.core.data.remote.entity.RemoteChapter
import io.igist.core.data.remote.toResultUpdate
import io.igist.core.data.remote.webservice.ChapterWebservice
import io.igist.core.domain.contract.ChapterRepository
import io.igist.core.domain.model.Book
import io.igist.core.domain.model.Chapter
import retrofit2.Response
import java.util.concurrent.CancellationException

/**
 * Implementation of [ChapterRepository] that parses a PLIST file from the network to get one or
 * all chapters from a book.
 */
class ChapterRepositoryImpl(

    private val chapterDao: ChapterDao,

    private val chapterWebservice: ChapterWebservice

): ChapterRepository {

    // region Properties

    /**
     * The current [ChaptersTask].
     */
    private var chaptersTask: ChaptersTask? = null

    /**
     * A [LiveData] containing chapter list updates.
     */
    private val chaptersData: MediatorLiveData<DataUpdate<List<Chapter>, List<Chapter>>> =
        MediatorLiveData()

    // endregion Properties

    // region Methods

    /**
     * Returns a [LiveData] containing chapter list updates.
     */
    override fun getChapters(
        bookId: Long,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<List<Chapter>, List<Chapter>>> {
        chaptersTask?.cancel(true)

        ChaptersTask(chaptersData, chapterDao, chapterWebservice, alwaysFetch).apply {
            chaptersTask = this
            executeOnExecutorAsLiveData(AsyncTask.THREAD_POOL_EXECUTOR, bookId)
        }

        return chaptersData
    }

    /**
     * Returns a [LiveData] containing [Chapter] updates.
     */
    override fun getChapter(
        bookId: Long,
        chapterNumber: Int,
        alwaysFetch: Boolean
    ): LiveData<DataUpdate<Book, Book>> {
        TODO("not implemented")
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * A [DataTaskinator] that retrieves a list of [Book]s.
     */
    private class ChaptersTask(

        private val chaptersData: MediatorLiveData<DataUpdate<List<Chapter>, List<Chapter>>>,

        private val chapterDao: ChapterDao,

        private val chapterWebservice: ChapterWebservice,

        private val alwaysFetch: Boolean = true

    ) : DataTaskinator<Long, List<Chapter>, List<Chapter>>() {

        // region Inherited methods

        override fun onPreExecute() {
            super.onPreExecute()
            chaptersData.addSource(liveData) {
                chaptersData.value = it
            }
        }

        override fun doInBackground(vararg params: Long?): ResultUpdate<List<Chapter>, List<Chapter>> {
            // Extract arguments from params
            val bookId: Long = params.getOrNull(0)
                ?: throw IllegalArgumentException("No book ID passed to ChaptersTask")

            // Retrieve any cached chapter(s)
            val localChapters: List<LocalChapter>? = chapterDao.retrieveAll(bookId)
            var chapters: List<Chapter>? = localChapters.toChaptersOrNull()

            // Check if cancelled
            if (isCancelled) return FailureUpdate(chapters, CancellationException(), data)

            // Fetch the latest chapters
            if (chapters.isNullOrEmpty() || alwaysFetch) {
                // If we have cached chapters, publish them
                if (!chapters.isNullOrEmpty()) publishProgress(chapters)

                if (isCancelled) return FailureUpdate(chapters, CancellationException(), data)

                val update: ResultUpdate<Void, Response<List<RemoteChapter>>> =
                    chapterWebservice.chapters(bookId).toResultUpdate()

                Log.d("ChapterRepositoryImp", "update=$update")

                // Check if cancelled or failure
                when {
                    isCancelled -> return FailureUpdate(chapters, CancellationException(), data)
                    update is FailureUpdate ->
                        return FailureUpdate(chapters, update.e, data)
                }

                update.result?.body()?.run {
                    // Convert & insert remote Api into the local database
                    val localChapters = this.toLocalChapters(bookId)
                    chapterDao.insertAll(localChapters)

                    // Re-retrieve the newly-inserted Api from the local database
                    chapters = chapterDao.retrieveAll(bookId).toChaptersOrNull()
                }
            }

            return SuccessUpdate(chapters)
        }

        override fun onPostExecute(result: ResultUpdate<List<Chapter>, List<Chapter>>?) {
            super.onPostExecute(result)
            chaptersData.removeSource(liveData)
        }

        override fun onCancelled() {
            super.onCancelled()
            chaptersData.removeSource(liveData)
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
