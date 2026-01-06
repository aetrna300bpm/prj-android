package com.alpha.books_explorer.domain.usecase.FavList

import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository

class AddIntoFavListUseCase(
    private val repository: BookRepository,
) {
    suspend fun invoke(book: Book) {
        repository.addIntoFavListBooks(book)
    }
}
