package com.alpha.books_explorer.domain.usecase

import androidx.paging.PagingData
import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetBooksUseCase(
    private val repository: BookRepository,
) {
    fun invoke(query: String): Flow<List<Book>> {
        return repository.getBooks(query)
    }

    fun invokePaging(
        query: String,
        filter: String? = null,
        orderBy: String? = null,
        printType: String? = null
    ): Flow<PagingData<Book>> {
        return repository.getBooksFromPaging(query, filter, orderBy, printType)
    }
}
