package com.alpha.books_explorer.domain.usecase.readingList

import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository

class AddIntoReadingListUseCase(
    private val repository: BookRepository,
) {
    suspend fun invoke(book: Book) {
        repository.addIntoReadingListBooks(book)
    }
}
