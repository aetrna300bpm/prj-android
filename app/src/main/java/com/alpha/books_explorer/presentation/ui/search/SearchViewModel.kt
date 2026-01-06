package com.alpha.books_explorer.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.alpha.books_explorer.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel(
    private val getBooksUseCase: GetBooksUseCase,
) : ViewModel() {

    private val _searchBookList = MutableStateFlow(SearchUiState())
    val searchBookList: StateFlow<SearchUiState> = _searchBookList
    
    private var lastQuery: String = "Android"
    
    // Default filters
    var currentFilter: String? = null
    var currentOrderBy: String? = "relevance"
    var currentPrintType: String? = "all"

    init {
        loadBooks(lastQuery)
    }

    fun loadBooks(query: String) {
        lastQuery = query
        val pagingFlow = getBooksUseCase.invokePaging(
            query,
            filter = currentFilter,
            orderBy = currentOrderBy,
            printType = currentPrintType
        ).cachedIn(viewModelScope)
        
        _searchBookList.value = SearchUiState(books = pagingFlow)
    }
    
    fun updateFilters(filter: String?, orderBy: String?, printType: String?) {
        currentFilter = filter
        currentOrderBy = orderBy
        currentPrintType = printType
        loadBooks(lastQuery)
    }
}
