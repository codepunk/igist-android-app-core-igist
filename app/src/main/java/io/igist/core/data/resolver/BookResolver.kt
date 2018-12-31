package io.igist.core.data.resolver

import com.codepunk.doofenschmirtz.util.taskinator.DataResolvinator
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.local.entity.LocalBook
import io.igist.core.data.mapper.toBookOrNull
import io.igist.core.data.mapper.toLocalBookOrNull
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.data.remote.toResult
import io.igist.core.data.remote.webservice.BookWebservice
import io.igist.core.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BookResolver @Inject constructor(

    private val bookDao: BookDao,

    private val bookWebservice: BookWebservice

) : DataResolvinator<Long?, LocalBook?, RemoteBook?, Book?>() {

    private var bookId: Long = 0L

    var book: Book? = null
        private set

    override fun processParams(params: Array<out Long?>): String? {
        bookId = params.getOrNull(0) ?: return "No book ID passed to BookResolver"
        return null
    }

    override fun retrieveLocal(params: Array<out Long?>): LocalBook? = bookDao.retrieve(bookId)

    override fun localToDomain(local: LocalBook?): Book? = local.toBookOrNull()

    override fun onCache(cached: Book?) {
        book = cached
    }

    override fun shouldAlwaysFetch(alwaysFetch: Boolean): BookResolver =
        super.shouldAlwaysFetch(alwaysFetch) as BookResolver

    override fun fetchRemote(params: Array<out Long?>): RemoteBook? =
        bookWebservice.book(bookId).toResult()

    override fun remoteToLocal(remote: RemoteBook?): LocalBook? = remote.toLocalBookOrNull()

    override fun cacheLocal(local: LocalBook?) {
        local?.run {
            bookDao.upsert(this)
        } ?: bookDao.delete(bookId)
    }

}
