package com.alpha.books_explorer.domain.repository

import androidx.paging.PagingData
import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getBooks(query: String): Flow<List<Book>>

    fun getBooksFromPaging(
        query: String,
        filter: String? = null,
        orderBy: String? = null,
        printType: String? = null
    ): Flow<PagingData<Book>>

    fun getBookById(id: String): Flow<Book>
    
    fun getBookFromReadingList(id: String): Flow<Book>

    suspend fun addIntoFavListBooks(book: Book)

    suspend fun addIntoReadingListBooks(book: Book)

    suspend fun deleteFromFavListBooks(book: Book)

    suspend fun deleteFromReadingListBooks(book: Book)

    fun isBookPresentInFavList(book: Book): Flow<Boolean>

    fun isBookPresentInReadingList(book: Book): Flow<Boolean>

    fun getFavListBooks(): Flow<List<Book>>

    fun getReadingListBooks(): Flow<List<Book>>
    
    // Notes
    fun getNotesForBook(bookId: String): Flow<List<NoteEntity>>
    
    suspend fun addNote(note: NoteEntity)
    
    suspend fun deleteNote(note: NoteEntity)
    
    // Stats
    fun getReadingStats(): Flow<Map<String, Int>>
    
    // User Profile
    fun getUserName(): Flow<String>
    
    suspend fun saveUserName(firstName: String, lastName: String)
}
