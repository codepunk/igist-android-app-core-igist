/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.data.mapper

import io.igist.core.data.local.entity.*
import io.igist.core.data.remote.entity.RemoteContentFile
import io.igist.core.data.remote.entity.RemoteContentList
import io.igist.core.data.remote.entity.RemoteStoreItem
import io.igist.core.domain.model.*

// region Methods

// region LocalContentList mappers

/**
 * Converts a [LocalContentList] to a domain [ContentList].
 */
fun LocalContentList.toContentList(
    bookId: Long,
    localChapterImages: List<LocalContentFile>,
    localSputniks: List<LocalContentFile>,
    localBadges: List<LocalContentFile>,
    localStorefront: List<LocalContentFile>,
    localStoreDepartments: List<LocalStoreDepartment>,
    localStoreCollections: List<LocalStoreCollection>,
    localStoreItems: List<LocalStoreItem>,
    localCards: List<LocalCard>,
    localCardImages: List<LocalCardImage>
): ContentList = ContentList(
    bookId,
    appVersion,
    live,
    newestAppVersion,
    localChapterImages.toContentFiles(FileCategory.CHAPTER_IMAGE),
    localSputniks.toContentFiles(FileCategory.SPUTNIK),
    localBadges.toContentFiles(FileCategory.BADGE),
    localStorefront.toContentFiles(FileCategory.STOREFRONT),
    localStoreDepartments.toStoreData(localStoreCollections, localStoreItems),
    localCards.toCardData(localCardImages)
)

// endregion LocalContentList mappers

// region LocalContentFile mappers

/**
 * Converts a [LocalContentFile] to a domain [ContentFile].
 */
fun LocalContentFile.toContentFile(fileCategory: FileCategory): ContentFile =
    ContentFile(fileCategory, filename, date)

/**
 * Converts a nullable [LocalContentFile] to a nullable domain [ContentFile].
 */
fun LocalContentFile?.toContentFileOrNull(fileCategory: FileCategory): ContentFile? =
    this?.toContentFile(fileCategory)

/**
 * Converts a list of [LocalContentFile]s to a list of domain [ContentFile]s.
 */
fun List<LocalContentFile>.toContentFiles(fileCategory: FileCategory): List<ContentFile> =
    map { it.toContentFile(fileCategory) }

/**
 * Converts a nullable list of [LocalContentFile]s to a nullable list of domain [ContentFile]s.
 */
fun List<LocalContentFile>?.toContentFilesOrNull(fileCategory: FileCategory): List<ContentFile>? =
    this?.toContentFiles(fileCategory)

// endregion LocalContentFile mappers

// region LocalStore mappers

/**
 * Converts a list of [LocalStoreDepartment]s to a domain store data construct, converting
 * [localStoreCollections] and [localStoreItems] along the way as appropriate.
 */
fun List<LocalStoreDepartment>.toStoreData(
    localStoreCollections: List<LocalStoreCollection>,
    localStoreItems: List<LocalStoreItem>
): Map<String, List<Map<String, List<StoreItem>>>> {
    // Create the main store data map
    val storeDataMap = HashMap<String, List<Map<String, List<StoreItem>>>>()

    // Populate the main store data map; each item in "this" is a local store department
    this.forEach { localStoreDepartment ->
        // Create a new array list called "categoryList" and add it to the main data store map
        val categoryList = ArrayList<Map<String, List<StoreItem>>>()
        storeDataMap[localStoreDepartment.name] = categoryList

        // Filter out all local store collections associated with each department
        localStoreCollections.filter { it.departmentId == localStoreDepartment.id }.forEach {
            val collectionMap = HashMap<String, List<StoreItem>>()
            categoryList.add(collectionMap)

            // Filter out all local store items associated with this collection and convert/add
            // them to the domain collection map
            collectionMap[it.name] = localStoreItems.filter { localStoreItem ->
                localStoreItem.collectionId == it.id
            }.toStoreItems()
        }
    }

    // Return the populated map
    return storeDataMap
}

/**
 * Converts a [LocalStoreItem] to a domain [StoreItem].
 */
fun LocalStoreItem.toStoreItem(): StoreItem = StoreItem(
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

/**
 * Converts a nullable [LocalStoreItem] to a nullable domain [StoreItem].
 */
fun LocalStoreItem?.toStoreItemOrNull(): StoreItem? = this?.toStoreItem()

/**
 * Converts a list of [LocalStoreItem]s to a list of domain [StoreItem]s.
 */
fun List<LocalStoreItem>.toStoreItems(): List<StoreItem> = map { it.toStoreItem() }

/**
 * Converts a nullable list of [LocalStoreItem]s to a nullable list of domain [StoreItem]s.
 */
fun List<LocalStoreItem>?.toStoreItemsOrNull(): List<StoreItem>? = this?.toStoreItems()

// endregion LocalStore mappers

// region RemoteStore mappers

/**
 * Converts a [RemoteStoreItem] to a [LocalStoreItem].
 */
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

/**
 * Converts a nullable [RemoteStoreItem] to a nullable [LocalStoreItem].
 */
fun RemoteStoreItem?.toLocalStoreItemOrNull(collectionId: Long): LocalStoreItem? =
    this?.toLocalStoreItem(collectionId)

/**
 * Converts a list of [RemoteStoreItem]s to a list of [LocalStoreItem]s.
 */
fun List<RemoteStoreItem>.toLocalStoreItems(collectionId: Long): List<LocalStoreItem> =
    map { it.toLocalStoreItem(collectionId) }

/**
 * Converts a nullable list of [RemoteStoreItem]s to a nullable list of [LocalStoreItem]s.
 */
fun List<RemoteStoreItem>?.toLocalStoreItemsOrNull(collectionId: Long): List<LocalStoreItem>? =
    this?.toLocalStoreItems(collectionId)

// endregion RemoteStore mappers

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

// region LocalCard mappers

fun LocalCard.toCard(images: List<String>) = Card(name, bio, images, video)

/**
 * Converts a list of [LocalCard]s to a map of card names to doman [Card].
 */
fun List<LocalCard>.toCardData(
    localCardImages: List<LocalCardImage>
): Map<String, Card> {
    // Create the main store data map
    val cardDataMap = HashMap<String, Card>()

    // Populate the main store data map; each item in "this" is a local card
    this.forEach { localCard ->
        val filteredImages = localCardImages.filter { it.localCardId == localCard.id }
        val images: List<String> = filteredImages.map { it.imageName }
        cardDataMap[localCard.name] = localCard.toCard(images)
    }

    // Return the populated map
    return cardDataMap
}

// endregion LocalCard mappers

// region RemoteCard mappers

fun RemoteCard.toLocalCard(contentListId: Long, cardIndex: Int): LocalCard = LocalCard(
    contentListId,
    cardIndex,
    name,
    bio,
    video
)

// endregion RemoteCard mappers

// endregion Methods
