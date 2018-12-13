/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.domain.contract.BookRepository
import io.igist.core.domain.model.Book
import io.igist.core.presentation.loading.LoadingTask.LoadingProgress

class LoadingTask(

    private val bookRepository: BookRepository

) : AsyncTask<Long, LoadingProgress, Boolean>() {

    // region Properties

    private val loadingProgress: LoadingProgress = LoadingProgress()

    private val bookUpdate: MediatorLiveData<DataUpdate<Book, Book>> = MediatorLiveData()

    private var bookUpdateSource: LiveData<DataUpdate<Book, Book>>? = null
        set(value) {
            field?.run { bookUpdate.removeSource(this) }
            field = value
            field?.run {
                bookUpdate.addSource(this) {
                    bookUpdate.value = it
                }
            }
        }

    // endregion Properties

    // region Inherited methods

    override fun doInBackground(vararg params: Long?): Boolean {
        // Extract book ID from params
        val bookId: Long = params.getOrNull(0) ?: throw IllegalArgumentException(
            "No book ID supplied to ${LoadingTask::class.java.simpleName}"
        )

        bookUpdateSource = bookRepository.getBook(bookId).apply {
            observeForever {

            }
        }

        return true
    }

    // endregion Inherited methods

    // region Nested/inner classes

    class LoadingProgress(

        val progress: Int = 0,

        val max: Int = 0,

        val message: String = ""

    )

    // endregion Nested/inner classes

}
