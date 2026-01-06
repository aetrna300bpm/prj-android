package com.alpha.books_explorer.domain.usecase.notes

import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.domain.repository.BookRepository

class AddNoteUseCase(private val repository: BookRepository) {
    suspend fun invoke(note: NoteEntity) {
        repository.addNote(note)
    }
}
