package com.alpha.books_explorer.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.books_explorer.domain.model.ThemeMode
import com.alpha.books_explorer.domain.repository.BookRepository
import com.alpha.books_explorer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val bookRepository: BookRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val userName: StateFlow<String> = bookRepository.getUserName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "John Doe"
        )

    val readingListCount: StateFlow<Int> = bookRepository.getReadingListBooks()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
    val readingStats: StateFlow<Map<String, Int>> = bookRepository.getReadingStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
        
    val themeMode: StateFlow<ThemeMode> = settingsRepository.getThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun updateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            bookRepository.saveUserName(firstName, lastName)
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}
