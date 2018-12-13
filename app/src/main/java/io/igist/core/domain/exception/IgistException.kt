/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.exception

import io.igist.core.domain.model.ResultMessage
import io.igist.core.domain.model.ResultMessage.UnknownResultMessage

/**
 * A [RuntimeException] that contains a [ResultMessage].
 */
class IgistException : RuntimeException {

    // region Properties

    /**
     * A [ResultMessage] associated with this exception.
     */
    val resultMessage: ResultMessage

    // endregion Properties

    // region Constructors

    constructor(resultMessage: ResultMessage = UnknownResultMessage()) : super() {
        this.resultMessage = resultMessage
    }

    constructor(
        message: String?,
        resultMessage: ResultMessage = UnknownResultMessage()
    ) : super(message) {
        this.resultMessage = resultMessage
    }

    constructor(
        message: String?,
        cause: Throwable?,
        resultMessage: ResultMessage = UnknownResultMessage()
    ) : super(message, cause) {
        this.resultMessage = resultMessage
    }

    constructor(
        cause: Throwable?,
        resultMessage: ResultMessage = UnknownResultMessage()
    ) : super(cause) {
        this.resultMessage = resultMessage
    }

    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        resultMessage: ResultMessage = UnknownResultMessage()
    ) : super(message, cause, enableSuppression, writableStackTrace) {
        this.resultMessage = resultMessage
    }

    // endregion Constructors

    // region Inherited methods

    override fun toString(): String {
        return IgistException::class.java.simpleName + "(resultMessage=$resultMessage)"
    }

    // endregion Inherited methods

}
