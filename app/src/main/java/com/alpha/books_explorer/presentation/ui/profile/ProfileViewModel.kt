package com.alpha.books_explorer.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.books_explorer.domain.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    // Analysis: Count books in reading list
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

    fun loadUserProfile() {
        // In a real app, fetch from DataStore/API.
        // For now, we use the default values in UserProfile or keep current state.
    }

    fun updateFirstName(name: String) {
        _userProfile.value = _userProfile.value.copy(firstName = name)
    }

    fun updateLastName(name: String) {
        _userProfile.value = _userProfile.value.copy(lastName = name)
    }
    
    fun logout() {
        // Reset to default or clear data
        _userProfile.value = UserProfile(firstName = "", lastName = "", email = "")
    }
}

data class UserProfile(
    val firstName: String = "John",
    val lastName: String = "Doe",
    val email: String = "john.doe@example.com",
)
