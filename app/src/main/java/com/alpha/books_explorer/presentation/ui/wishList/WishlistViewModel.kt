package com.alpha.books_explorer.presentation.ui.wishList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.books_explorer.domain.usecase.readingList.FetchReadingListUseCase
import com.alpha.books_explorer.presentation.ui.home.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val getReadingListUseCase: FetchReadingListUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            // FetchReadingListUseCase reads from DB, no delay needed ideally, but keeping delay for UI feel if desired
            getReadingListUseCase.invoke()
                .catch { e ->
                    _uiState.value = HomeUiState(error = e.message ?: "Unknown error")
                }
                .collect { books ->
                    _uiState.value = HomeUiState(books = books)
                }
        }
    }
}
