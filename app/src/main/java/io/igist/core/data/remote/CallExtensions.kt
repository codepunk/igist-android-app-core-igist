/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote

import android.os.Bundle
import androidx.lifecycle.LiveData
import com.codepunk.doofenschmirtz.util.http.HttpStatusException
import com.codepunk.doofenschmirtz.util.taskinator.FailureUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ResultUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * Extension function that takes a [Response] from executing a [Call] and converts it into a
 * [ResultUpdate].
 *
 * This allows for ultra-concise code, such as the following example in
 * [DataTaskinator.doInBackground]:
 *
 * ```kotlin
 * override fun doInBackground(vararg params: Void?): DataUpdate<Void, User>? =
 *     myWebservice.getMyData().toResultUpdate()
 * ```
 *
 * In the above example, doInBackground will return an appropriate instance of
 * DataUpdate<Void, User>. This value will be stored in the DataTaskinator's [LiveData], which
 * can be observed from an Activity or other observer.
 */
fun <Progress, Result> Call<Result>.toResultUpdate(data: Bundle? = null):
        ResultUpdate<Progress, Response<Result>> {
    return try {
        val response = execute()
        when {
            response.isSuccessful ->
                SuccessUpdate<Progress, Response<Result>>(response).apply {
                    this.data = data
                }
            else -> FailureUpdate<Progress, Response<Result>>(
                // TODO Try to process response.errorBody()?.string()? Or can I just do that in the observer?
                response,
                HttpStatusException(response.code())
            ).apply {
                this.data = data
            }
        }
    } catch (e: IOException) {
        FailureUpdate<Progress, Response<Result>>(null, e).apply {
            this.data = data
        }
    }
}

fun <Result> Call<Result>.toResult(): Result? {
    return try {
        val response: Response<Result> = execute()
        when {
            response.isSuccessful -> response.body()
            else -> throw HttpStatusException(response.code())
        }
    } catch (e: IOException) {
        throw e
    }
}
