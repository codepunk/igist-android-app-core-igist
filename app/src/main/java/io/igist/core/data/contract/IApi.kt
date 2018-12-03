/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.contract

import io.igist.core.data.model.IgistMode

/**
 * A contract representing basic API information.
 */
interface IApi {

    /**
     * The API version.
     */
    val version: Int

    /**
     * A value that provides certain behavior for the app.
     */
    val igistMode: IgistMode

    /**
     * A link used if user agrees to do a survey (prompted when user completes the book).
     */
    val surveyLink: String

}
