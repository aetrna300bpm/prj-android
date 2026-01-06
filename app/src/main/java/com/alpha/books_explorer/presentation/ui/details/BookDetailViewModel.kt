package com.alpha.books_explorer.presentation.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.usecase.FavList.AddIntoFavListUseCase
import com.alpha.books_explorer.domain.usecase.FavList.IsBookPresentInFavListUserCase
import com.alpha.books_explorer.domain.usecase.FavList.RemoveFromFavListUseCase
import com.alpha.books_explorer.domain.usecase.GetBookByIdUserCase
import com.alpha.books_explorer.domain.usecase.notes.AddNoteUseCase
import com.alpha.books_explorer.domain.usecase.notes.DeleteNoteUseCase
import com.alpha.books_explorer.domain.usecase.notes.GetNotesForBookUseCase
import com.alpha.books_explorer.domain.usecase.readingList.AddIntoReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.GetBookFromReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.IsBookPresentInReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.RemoveFromReadingListUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val getBookById: GetBookByIdUserCase,
    private val getBookFromReadingList: GetBookFromReadingListUseCase,
    private val addIntoFavListUseCase: AddIntoFavListUseCase,
    private val isBookPresentInFavListUserCase: IsBookPresentInFavListUserCase,
    private val removeFromFavListUseCase: RemoveFromFavListUseCase,
    private val addIntoReadingListUseCase: AddIntoReadingListUseCase,
    private val isBookPresentInReadingListUseCase: IsBookPresentInReadingListUseCase,
    private val removeFromReadingListUseCase: RemoveFromReadingListUseCase,
    private val getNotesForBookUseCase: GetNotesForBookUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {
    private val _bookState = MutableStateFlow(BookDetailsUiState())
    val bookState: StateFlow<BookDetailsUiState> = _bookState

    private val _checkReadinglistItem = MutableStateFlow(false)
    val checkReadinglistItem: StateFlow<Boolean> = _checkReadinglistItem

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes

    fun checkReadinglistItem(book: Book?) {
        if (book == null) {
            _checkReadinglistItem.value = false
            return
        }
        viewModelScope.launch {
            isBookPresentInReadingListUseCase.invoke(book).collect { isPresent ->
                _checkReadinglistItem.value = isPresent
            }
        }
    }

    fun fetchBookById(bookId: String) {
        viewModelScope.launch {
            _bookState.value = BookDetailsUiState(isLoading = true)
            loadNotes(bookId)
            
            getBookFromReadingList.invoke(bookId)
                .catch { 
                    loadFromApi(bookId)
                }
                .collect { dbBook ->
                    _bookState.value = BookDetailsUiState(book = dbBook)
                    checkReadinglistItem(dbBook)
                }
        }
        loadFromApi(bookId)
        loadFromDb(bookId)
    }
    
    private fun loadFromApi(bookId: String) {
        viewModelScope.launch {
            getBookById.invoke(bookId)
                .catch { e ->
                     if (_bookState.value.book == null) {
                         _bookState.value = BookDetailsUiState(error = e.message)
                     }
                }
                .collect { apiBook ->
                    if (!_checkReadinglistItem.value) {
                         _bookState.value = BookDetailsUiState(book = apiBook)
                         checkReadinglistItem(apiBook)
                    }
                }
        }
    }
    
    private fun loadFromDb(bookId: String) {
        viewModelScope.launch {
            getBookFromReadingList.invoke(bookId).collect { dbBook ->
                _bookState.value = BookDetailsUiState(book = dbBook)
                _checkReadinglistItem.value = true
            }
        }
    }
    
    private fun loadNotes(bookId: String) {
        viewModelScope.launch {
            getNotesForBookUseCase.invoke(bookId).collect {
                _notes.value = it
            }
        }
    }

    fun addToReadinglist(book: Book) {
        viewModelScope.launch {
            addIntoReadingListUseCase.invoke(book)
            checkReadinglistItem(book)
            loadFromDb(book.id)
        }
    }

    fun removeFromReadingList(book: Book) {
        viewModelScope.launch {
            removeFromReadingListUseCase.invoke(book)
            checkReadinglistItem(book)
        }
    }
    
    // Auto-save rating
    fun updateRating(book: Book, rating: Float) {
        val updatedBook = book.copy(rating = rating)
        addToReadinglist(updatedBook)
    }
    
    fun updateStatus(book: Book, status: String) {
        val updatedBook = book.copy(readingStatus = status)
        addToReadinglist(updatedBook)
    }
    
    fun addNote(bookId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val note = NoteEntity(
                bookId = bookId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            addNoteUseCase.invoke(note)
            // Note list automatically updates via Flow
        }
    }
    
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            deleteNoteUseCase.invoke(note)
        }
    }
}
