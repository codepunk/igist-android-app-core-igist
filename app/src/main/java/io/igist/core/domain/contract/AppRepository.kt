/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.contract

import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import io.igist.core.domain.model.Api

interface AppRepository {

    // region Methods

    fun getApi(bookId: Long, apiVersion: Int): LiveData<DataUpdate<Api, Api>>

    // endregion Methods

}
