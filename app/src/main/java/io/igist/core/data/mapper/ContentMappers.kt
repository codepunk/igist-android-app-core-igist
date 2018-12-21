/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.LocalContentFile
import io.igist.core.data.local.entity.LocalContentList
import io.igist.core.data.local.entity.LocalStoreDepartment
import io.igist.core.data.local.entity.LocalStoreItem
import io.igist.core.data.remote.entity.RemoteContentFile
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteStoreItem
import io.igist.core.domain.model.ContentFile
import io.igist.core.domain.model.ContentList
import io.igist.core.domain.model.FileCategory
import io.igist.core.domain.model.StoreItem

// region Methods

// region LocalContentFile mappers

/**
 * Converts a [LocalContentFile] to a domain [ContentFile].
 */
fun LocalContentFile.toContentFile(): ContentFile =
    ContentFile(filename, date)

/**
 * Converts a nullable [LocalContentFile] to a nullable domain [ContentFile].
 */
fun LocalContentFile?.toContentFileOrNull(): ContentFile? = this?.toContentFile()

/**
 * Converts a list of [LocalContentFile]s to a list of domain [ContentFile]s.
 */
fun List<LocalContentFile>.toContentFiles(): List<ContentFile> = map { it.toContentFile() }

/**
 * Converts a nullable list of [LocalContentFile]s to a nullable list of domain [ContentFile]s.
 */
fun List<LocalContentFile>?.toContentFilesOrNull(): List<ContentFile>? = this?.toContentFiles()

// endregion LocalContentFile mappers

// region LocalStore mappers

/**
 * Converts a list of [LocalStoreDepartment]s to a domain store data collection.
 */
fun List<LocalStoreDepartment>.toStoreData(): Map<String, List<Map<String, List<StoreItem>>>> {
    val map = HashMap<String, List<Map<String, List<StoreItem>>>>()
    return map
}

fun RemoteStoreItem.toLocalStoreItem(collectionId: Long): LocalStoreItem = LocalStoreItem(
    collectionId,
    contentId,
    contentLink,
    storeIcon,
    currency,
    price,
    title,
    type,
    order,
    description
)

fun RemoteStoreItem?.toLocalStoreItemOrNull(collectionId: Long): LocalStoreItem? =
    this?.toLocalStoreItem(collectionId)

fun List<RemoteStoreItem>.toLocalStoreItems(collectionId: Long): List<LocalStoreItem> =
    map { it.toLocalStoreItem(collectionId) }

fun List<RemoteStoreItem>?.toLocalStoreItemsOrNull(collectionId: Long): List<LocalStoreItem>? =
    this?.toLocalStoreItems(collectionId)

// endregion LocalStore mappers

// region RemoteContentFile mappers

/**
 * Converts a [RemoteContentFile] to a [LocalContentFile].
 */
fun RemoteContentFile.toLocalContentFile(
    contentListId: Long,
    category: FileCategory
): LocalContentFile = LocalContentFile(contentListId, category, filename, date)

/**
 * Converts a nullable [RemoteContentFile] to a nullable [LocalContentFile].
 */
fun RemoteContentFile?.toLocalContentFileOrNull(
    contentListId: Long,
    category: FileCategory
): LocalContentFile? = this?.toLocalContentFile(contentListId, category)

/**
 * Converts a list of [RemoteContentFile] to a list of [LocalContentFile].
 */
fun List<RemoteContentFile>.toLocalContentFiles(
    contentListId: Long,
    category: FileCategory
): List<LocalContentFile> = map { it.toLocalContentFile(contentListId, category) }

/**
 * Converts a nullable list of [RemoteContentFile] to a nullable list of [LocalContentFile].
 */
fun List<RemoteContentFile>?.toLocalContentFilesOrNull(
    contentListId: Long,
    category: FileCategory
): List<LocalContentFile>? = this?.mapIndexed { index, remoteContentFile ->
    remoteContentFile.toLocalContentFile(contentListId, category)
}

// endregion RemoteContentFile mappers

// region RemoteContentList mappers

/**
 * Converts a [LocalContentList] to a domain [ContentList].
 */
fun LocalContentList.toContentList(
    localChapterImages: List<LocalContentFile>,
    localSputniks: List<LocalContentFile>,
    localBadges: List<LocalContentFile>,
    localStorefront: List<LocalContentFile>,
    localStoreDepartments: List<LocalStoreDepartment>
): ContentList = ContentList(
    appVersion,
    live,
    newestAppVersion,
    localChapterImages.toContentFiles(),
    localSputniks.toContentFiles(),
    localBadges.toContentFiles(),
    localStorefront.toContentFiles(),
    localStoreDepartments.toStoreData(),
    null /* TODO */
)

/**
 * Converts a [RemoteContentList] to a [LocalContentList].
 */
fun RemoteContentList.toLocalContentList(bookId: Long, num: Int) = LocalContentList(
    bookId,
    appVersion,
    num,
    live,
    newestAppVersion
)

/**
 * Converts a list of [RemoteContentList] to a list of [LocalContentList].
 */
fun List<RemoteContentList>.toLocalContentLists(bookId: Long): List<LocalContentList> =
    mapIndexed { index, remoteContentList ->
        remoteContentList.toLocalContentList(bookId, index + 1)
    }

// endregion RemoteContentList mappers

// endregion Methods
