/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.LocalBook
import io.igist.core.data.remote.entity.RemoteBook
import io.igist.core.domain.model.Book

/**
 * Converts a [LocalBook] to a domain [Book].
 */
fun LocalBook.toBook(): Book =
    Book(id, title, description, previewImageName, apiVersion, appVersion, locked, plistFile)

/**
 * Converts a nullable [LocalBook] to a nullable domain [Book].
 */
fun LocalBook?.toBookOrNull(): Book? = this?.toBook()

/**
 * Converts a [List] of [LocalBook]s to a list of domain [Book]s.
 */
fun List<LocalBook>.toBooks(): List<Book> = map { it.toBook() }

/**
 * Converts a [RemoteBook] to a [LocalBook].
 */
fun RemoteBook.toLocalBook(): LocalBook =
    LocalBook(id, title, description, previewImageName, apiVersion, appVersion, locked, plistFile)

/**
 * Converts a nullable [RemoteBook] to a nullable [LocalBook].
 */
@Suppress("UNUSED")
fun RemoteBook?.toLocalBookOrNull(): LocalBook? = this?.toLocalBook()

/**
 * Converts a [List] of [RemoteBook]s to a list of [LocalBook]s.
 */
fun List<RemoteBook>.toLocalBooks(): List<LocalBook> = map { it.toLocalBook() }
