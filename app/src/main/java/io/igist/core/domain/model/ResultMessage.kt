/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.domain.model

import android.content.Context
import androidx.annotation.StringRes
import io.igist.core.R

/**
 * Class that enumerates known messages returned from the server.
 */
open class ResultMessage private constructor(

    /**
     * The String value associated with the [ResultMessage].
     */
    val value: String?,

    /**
     * The resource ID associated with the [ResultMessage], for displaying a localized/
     * user-friendly message.
     */
    @StringRes val resId: Int = R.string.result_message_default,

    /**
     * The value that will be returned by the [toString] method.
     */
    val toString: String = "${ResultMessage::class.java.simpleName}(value=$value)"

) {

    // region Inherited methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultMessage) return false

        if (value != other.value) return false
        if (resId != other.resId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + resId
        return result
    }

    override fun toString(): String = toString

    // endregion Inherited methods

    // region Methods

    /**
     * Adds this ResultMessage to [lookupMap] for quick lookup.
     */
    private fun addToLookup(): ResultMessage {
        lookupMap[value] = this
        return this
    }

    /**
     * Returns the string resource associated with this ResultMessage.
     */
    open fun getString(context: Context): String = context.getString(resId)

    // endregion Methods

    // region Companion object

    @Suppress("UNUSED")
    companion object {

        // region Properties

        /**
         * A [HashMap] for speedy lookup.
         */
        private val lookupMap = HashMap<String?, ResultMessage>()

        /**
         * A ResultMessage indicating an existing user with the same username and/or email.
         */
        @JvmStatic
        val ALREADY_EXISTS: ResultMessage = ResultMessage(
            "username or email already exist",
            R.string.result_message_already_exists,
            "ALREADY_EXISTS"
        ).addToLookup()

        /**
         * A ResultMessage indicating an invalid beta key.
         */
        @JvmStatic
        val BAD_KEY: ResultMessage = ResultMessage(
            "bad key",
            R.string.result_message_bad_key,
            "BAD_KEY"
        ).addToLookup()

        /**
         * A ResultMessage indicating that a beta key is required to read the book.
         */
        @JvmStatic
        val BETA_KEY_REQUIRED: ResultMessage = ResultMessage(
            "beta key required",
            R.string.result_message_beta_key_required,
            "BETA_KEY_REQUIRED"
        ).addToLookup()

        /**
         * A ResultMessage indicating a null message.
         */
        @JvmStatic
        val NULL_MESSAGE: ResultMessage = ResultMessage(
            null,
            R.string.result_message_null_message,
            "NULL_MESSAGE"
        ).addToLookup()

        /**
         * A ResultMessage indicating a registration error.
         */
        @JvmStatic
        val REGISTRATION_ERROR: ResultMessage = ResultMessage(
            "registration error",
            R.string.result_message_registration_error,
            "REGISTRATION_ERROR"
        ).addToLookup()

        /**
         * A ResultMessage indicating a successful operation.
         */
        @JvmStatic
        val SUCCESS: ResultMessage = ResultMessage(
            "success",
            R.string.result_message_success,
            "SUCCESS"
        ).addToLookup()

        // endregion Properties

        // region Methods

        /**
         * Returns a predefined [ResultMessage] if the [value] matches one of the predefined values,
         * otherwise it creates a new UnknownResultMessage using the supplied value.
         */
        fun lookup(value: String?): ResultMessage {
            val predefined = lookupMap[value]
            return when (predefined) {
                null -> UnknownResultMessage(value)
                else -> predefined
            }
        }

        // endregion Methods

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * A specialized implementation of ResultMessage that indicates an unrecognized message.
     */
    class UnknownResultMessage(
        value: String? = null
    ) : ResultMessage(
        value = value,
        toString = "${UnknownResultMessage::class.java.simpleName}(value=$value)"
    ) {

        // region Inherited methods

        override fun getString(context: Context): String = when (value) {
            null -> context.getString(R.string.result_message_unknown_no_value)
            else -> context.getString(R.string.result_message_unknown, value)
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
