package com.alpha.books_explorer.domain.model

data class Book(
    val id: String,
    val volumeInfo: VolumeInfo,
    // User specific fields (from local DB)
    val readingStatus: String? = null, // "To Read", "Reading", "Finished"
    val rating: Float? = null,
    val notes: String? = null
)

data class VolumeInfo(
    val title: String?,
    val subtitle: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val previewLink: String?
)

data class ImageLinks(
    val thumbnail: String?,
)
