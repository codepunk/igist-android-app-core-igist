package io.igist.core.data.resolver

import com.codepunk.doofenschmirtz.util.http.HttpStatus
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.taskinator.DataResolvinator
import io.igist.core.data.local.dao.BookDao
import io.igist.core.data.local.entity.LocalBook
import io.igist.core.data.mapper.toBooks
import io.igist.core.data.mapper.toLocalBooks
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.data.remote.toResult
import io.igist.core.data.remote.webservice.BookWebservice
import io.igist.core.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BooksResolver @Inject constructor(

    private val bookDao: BookDao,

    private val bookWebservice: BookWebservice

) : DataResolvinator<Void?, List<LocalBook>, List<RemoteBook>, List<Book>>() {

    var books: List<Book> = emptyList()
        private set

    override fun retrieveLocal(params: Array<out Void?>): List<LocalBook> = bookDao.retrieveAll()

    override fun localToDomain(local: List<LocalBook>): List<Book> = local.toBooks()

    override fun onCache(cached: List<Book>) {
        books = cached
    }

    override fun shouldAlwaysFetch(alwaysFetch: Boolean): BooksResolver =
        super.shouldAlwaysFetch(alwaysFetch) as BooksResolver

    override fun fetchRemote(params: Array<out Void?>): List<RemoteBook> =
        bookWebservice.books().toResult() ?: throw HttpStatusException(HttpStatus.NOT_FOUND)

    override fun remoteToLocal(remote: List<RemoteBook>): List<LocalBook> = remote.toLocalBooks()

    override fun cacheLocal(local: List<LocalBook>) {
        bookDao.upsertAll(local)
    }

}
