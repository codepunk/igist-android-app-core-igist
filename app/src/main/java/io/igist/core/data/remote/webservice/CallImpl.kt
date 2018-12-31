package io.igist.core.data.remote.webservice

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback

/**
 * An implementation of [Call] that throws a [NotImplementedError] for all methods
 * except for [execute].
 */
abstract class CallImpl<T> : Call<T> {

    var isCancelled: Boolean = false
        private set

    override fun enqueue(callback: Callback<T>) = throw NotImplementedError()

    override fun isExecuted(): Boolean = throw NotImplementedError()

    override fun clone(): Call<T> = throw NotImplementedError()

    override fun isCanceled(): Boolean = throw NotImplementedError()

    override fun cancel() {
        isCancelled = true
    }

    override fun request(): Request = throw NotImplementedError()

}
