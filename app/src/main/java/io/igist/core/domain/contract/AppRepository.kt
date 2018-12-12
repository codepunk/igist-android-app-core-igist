/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.domain.model.Api

/**
 * An interface that establishes the app repository contract.
 */
interface AppRepository {

    // region Methods

    /**
     * Gets a API data for the given [bookId] and [apiVersion]. If [alwaysFetch] is set, the
     * remote version will be fetched regardless of whether a cached version exists.
     */
    fun getApi(
        bookId: Long,
        apiVersion: Int,
        alwaysFetch: Boolean = true
    ): LiveData<DataUpdate<Api, Api>>

    // endregion Methods

}
