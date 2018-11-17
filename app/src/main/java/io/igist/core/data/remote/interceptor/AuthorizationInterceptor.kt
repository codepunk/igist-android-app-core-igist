/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.interceptor

import io.igist.core.data.remote.HEADER_NAME_AUTHORIZATION
import io.igist.core.data.remote.HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER
import io.igist.core.session.UserSessionManager
import dagger.Lazy
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class that intercepts Retrofit requests and looks for a header with a name
 * of [HEADER_NAME_AUTHORIZATION] ("Authorization"). If found, any instance in the value matching
 * [HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER] will be replaced with the authToken (if any) currently
 * stored in [UserSessionManager].
 */
@Singleton
class AuthorizationInterceptor @Inject constructor(

    /**
     * The userSession manager used for managing a user userSession. Lazy to avoid circular reference in
     * Dagger.
     */
    private val lazySessionManager: Lazy<UserSessionManager>

) : Interceptor {

    // region Implemented methods

    /**
     * Implementation of [Interceptor]. Looks for a header with a name of
     * [HEADER_NAME_AUTHORIZATION] ("Authorization") and replaces any instance of
     * [HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER] in the value with the authToken (if any) currently
     * stored in [UserSessionManager].
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken = lazySessionManager.get().userSession?.authToken ?: ""
        val originalRequest = chain.request()
        val request = originalRequest.header(HEADER_NAME_AUTHORIZATION)?.let { value ->
            originalRequest.newBuilder()
                .header(
                    HEADER_NAME_AUTHORIZATION,
                    value.replace(
                        HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER,
                        authToken,
                        true
                    )
                )
                .build()
        } ?: originalRequest
        return chain.proceed(request)
    }

    // endregion Implemented methods

}
