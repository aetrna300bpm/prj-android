package com.alpha.books_explorer.data.mappers

import com.alpha.books_explorer.data.local.entities.BookEntity
import com.alpha.books_explorer.data.local.entities.ReadingListEntity
import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.model.ImageLinks
import com.alpha.books_explorer.domain.model.VolumeInfo

fun BookEntity.toBook(): Book {
    val image = ImageLinks(thumbnail = thumbnail)
    val volume =
        VolumeInfo(
            title = title,
            subtitle = subtitle,
            authors = authors,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            imageLinks = image,
            previewLink = null // FavList doesn't store previewLink yet
        )
    return Book(
        id = id,
        volumeInfo = volume,
    )
}

fun ReadingListEntity.toBook(): Book {
    val image = ImageLinks(thumbnail = thumbnail)
    val volume =
        VolumeInfo(
            title = title,
            subtitle = subtitle,
            authors = authors,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            imageLinks = image,
            previewLink = previewLink
        )
    return Book(
        id = id,
        volumeInfo = volume,
        readingStatus = readingStatus,
        rating = rating,
        notes = notes
    )
}

fun Book.toBookEntity(): BookEntity {
    return BookEntity(
        id = this.id,
        title = this.volumeInfo.title,
        subtitle = this.volumeInfo.subtitle,
        authors = this.volumeInfo.authors,
        publisher = this.volumeInfo.publisher,
        publishedDate = this.volumeInfo.publishedDate,
        description = this.volumeInfo.description,
        thumbnail = this.volumeInfo.imageLinks?.thumbnail,
    )
}

fun Book.toReadingListEntity(): ReadingListEntity {
    return ReadingListEntity(
        id = this.id,
        title = this.volumeInfo.title,
        subtitle = this.volumeInfo.subtitle,
        authors = this.volumeInfo.authors,
        publisher = this.volumeInfo.publisher,
        publishedDate = this.volumeInfo.publishedDate,
        description = this.volumeInfo.description,
        thumbnail = this.volumeInfo.imageLinks?.thumbnail,
        readingStatus = this.readingStatus ?: "To Read",
        rating = this.rating ?: 0f,
        notes = this.notes,
        previewLink = this.volumeInfo.previewLink
    )
}
