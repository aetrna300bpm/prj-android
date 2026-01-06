package com.alpha.books_explorer.domain.usecase.readingList

import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetBookFromReadingListUseCase(
    private val repository: BookRepository,
) {
    fun invoke(id: String): Flow<Book> {
        return repository.getBookFromReadingList(id)
    }
}
