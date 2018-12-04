/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.igist.core.data.task.*
import io.igist.core.data.local.dao.ApiDao
import io.igist.core.data.local.entity.ApiEntity
import io.igist.core.data.model.Api
import io.igist.core.data.remote.webservice.AppWebservice
import io.igist.core.data.task.toDataUpdate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository for fetching app-level information and implementing the onboarding/loading process.
 */
@Singleton
class LoadingRepository @Inject constructor(

    private val appDao: ApiDao,

    private val appWebservice: AppWebservice

) {

    // region Methods

    /**
     * A method for getting api data, wrapped in [DataUpdate]/[LiveData].
     */
    @SuppressLint("StaticFieldLeak")
    fun getApiUpdateData(apiVersion: Int): LiveData<DataUpdate<Int, Api>> {
        val task = object : DataTask<Void, Int, Api>() {
            override fun generateUpdate(vararg params: Void?): DataUpdate<Int, Api> {

                // TODO Query the database first?
                /* THOUGHTS
                 * I might want to take Response out of DataUpdate.
                 * So that might look something like --
                 * Neither SuccessUpdate nor FailureUpdate has Response property
                 * BUT when calling a webservice, the DataUpdate might be
                 * DataUpdate<Void, Response<Api>>.
                 *
                 * That's going to affect a few areas, like toDataUpdate, etc. etc. etc.
                 */

                val apiEntity = appDao.retrieve(apiVersion)
                var api: Api? = when (apiEntity) {
                    null -> null
                    else -> Api(apiEntity)
                }
                if (api == null) {
                    val apiUpdate: DataUpdate<Int, Api> =
                        appWebservice.api(apiVersion).toDataUpdate()
                    when (apiUpdate) {
                        is FailureUpdate -> return apiUpdate
                        is SuccessUpdate -> {

                            apiUpdate.result?.apply {
                                val result = appDao.insert(ApiEntity(this))
                                val apiEntity = appDao.retrieve(apiVersion)

                                // return SuccessUpdate<Int, Api>()
                                return apiUpdate
                            }

                        }
                        else -> { return apiUpdate /* ??? */ }
                    }
                }

                return appWebservice.api(apiVersion).toDataUpdate()
            }
        }
        return task.fetchOnExecutor()
    }

    // endregion Methods

}
