package com.alpha.books_explorer.domain.usecase.notes

import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetNotesForBookUseCase(private val repository: BookRepository) {
    fun invoke(bookId: String): Flow<List<NoteEntity>> {
        return repository.getNotesForBook(bookId)
    }
}
