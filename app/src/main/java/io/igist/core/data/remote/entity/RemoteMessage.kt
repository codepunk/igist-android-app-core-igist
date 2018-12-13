/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.remote.entity

import com.squareup.moshi.Json
import io.igist.core.domain.model.ResultMessage

/**
 * Remote implementation of a data class representing a message returned from the server.
 */
data class RemoteMessage(

    /**
     * The message.
     */
    @field:Json(name = "message")
    val message: String? = ResultMessage.SUCCESS.toString


) {

    // region Properties

    /**
     * Backing property for [resultMessage] so the lookup only occurs once.
     */
    @Transient
    private lateinit var _resultMessage: ResultMessage

    /**
     * The [ResultMessage] associated with the message, if one is found.
     */
    val resultMessage: ResultMessage
        // NOTE: Initially this was attempted using a lazy directive but this resulted in an
        // IllegalStateException so we'll use a backing property instead.
        // NOTE 2: If we always want to treat null message as success, we can call
        // lookup(message ?: ResultMessage.SUCCESS.value) below instead.
        get() = if (!::_resultMessage.isInitialized) {
            ResultMessage.lookup(message ?: ResultMessage.SUCCESS.value).apply {
                _resultMessage = this
            }
        } else {
            _resultMessage
        }

    // endregion Properties

}
