/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.exception

class BadBetaKeyException : BetaKeyException {

    // region Constructors

    constructor() : super()

    constructor(s: String?) : super(s)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    // endregion Constructors

}
