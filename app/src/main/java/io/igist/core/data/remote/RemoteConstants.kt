/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

@file:JvmName("RemoteConstants")

package io.igist.core.data.remote

// region Constants

// Header names

/**
 * The "Accept" API header name.
 */
const val HEADER_NAME_ACCEPT = "Accept"

/**
 * The "Authorization" API header name.
 */
const val HEADER_NAME_AUTHORIZATION = "Authorization"

/**
 * The "Content-Type" API header name.
 */
const val HEADER_NAME_CONTENT_TYPE = "Content-Type"

// Header values

/**
 * A placeholder for an auth token in endpoints that require authentication.
 */
const val HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER = "\$authToken"

/**
 * The "application/json" header value.
 */
const val HEADER_VALUE_APPLICATION_JSON = "application/json"

// Header name/value pairs

/**
 * A name/value pair for accepting application/json responses.
 */
@Suppress("UNUSED")
const val HEADER_ACCEPT_APPLICATION_JSON = "$HEADER_NAME_ACCEPT: $HEADER_VALUE_APPLICATION_JSON"

/**
 * A name/value pair for bearer authorization header.
 */
@Suppress("UNUSED")
const val HEADER_AUTHORIZATION_BEARER =
    "$HEADER_NAME_AUTHORIZATION: Bearer $HEADER_VALUE_AUTH_TOKEN_PLACEHOLDER"

/**
 * A name/value pair for specifying application/json Content-Type.
 */
@Suppress("UNUSED")
const val HEADER_CONTENT_TYPE_APPLICATION_JSON =
    "$HEADER_NAME_CONTENT_TYPE: $HEADER_VALUE_APPLICATION_JSON"

// endregion Constants
