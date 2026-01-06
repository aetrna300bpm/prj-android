package com.alpha.books_explorer.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.alpha.books_explorer.data.local.FavBookDatabase
import com.alpha.books_explorer.data.local.dao.FavBookDao
import com.alpha.books_explorer.data.local.dao.ReadingListDao
import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.data.mappers.toBook
import com.alpha.books_explorer.data.mappers.toBookEntity
import com.alpha.books_explorer.data.mappers.toReadingListEntity
import com.alpha.books_explorer.data.paging.BooksPagingSource
import com.alpha.books_explorer.data.remote.BookApiService
import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class BookRepositoryImpl(
    private val api: BookApiService,
    private val localDao: FavBookDao,
    private val readingListDao: ReadingListDao,
    private val database: FavBookDatabase
) : BookRepository {
    
    private val noteDao = database.getNoteDao()

    override fun getBooks(query: String): Flow<List<Book>> = flow {
        val response = api.searchBooks(query, 0, 10)
        val books = response.items ?: emptyList()
        emit(books)
    }

    override fun getBooksFromPaging(
        query: String,
        filter: String?,
        orderBy: String?,
        printType: String?
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 20,
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { 
                BooksPagingSource(
                    api = api, 
                    query = query,
                    filter = filter,
                    orderBy = orderBy,
                    printType = printType
                ) 
            },
        ).flow
    }

    override fun getBookById(id: String): Flow<Book> = flow {
        val response = api.getBookById(id)
        emit(response)
    }

    override fun getBookFromReadingList(id: String): Flow<Book> = flow {
        val entity = readingListDao.getBookById(id)
        if (entity != null) {
            emit(entity.toBook())
        }
    }

    override suspend fun addIntoFavListBooks(book: Book) {
        localDao.insertFavBook(book.toBookEntity())
    }

    override suspend fun addIntoReadingListBooks(book: Book) {
        readingListDao.insertIntoReadingList(book.toReadingListEntity())
    }

    override suspend fun deleteFromFavListBooks(book: Book) {
        localDao.deleteBook(book.toBookEntity())
    }

    override suspend fun deleteFromReadingListBooks(book: Book) {
        readingListDao.deleteFromReadingList(book.toReadingListEntity())
    }

    override fun isBookPresentInFavList(book: Book): Flow<Boolean> = flow {
        val returnId = localDao.isBookPresent(book.id)
        returnId.let {
            if (it == null || it.isEmpty()) {
                emit(false)
            } else {
                emit(true)
            }
        }
    }

    override fun isBookPresentInReadingList(book: Book): Flow<Boolean> = flow {
        val returnId = readingListDao.isBookPresentInReadingList(book.id)
        returnId.let {
            if (it == null || it.isEmpty()) {
                emit(false)
            } else {
                emit(true)
            }
        }
    }

    override fun getFavListBooks(): Flow<List<Book>> = flow {
        emit(
            localDao.getFavBooks().map {
                it.toBook()
            },
        )
    }

    override fun getReadingListBooks(): Flow<List<Book>> = flow {
        emit(
            readingListDao.getReadingListBooks().map {
                it.toBook()
            },
        )
    }
    
    override fun getNotesForBook(bookId: String): Flow<List<NoteEntity>> {
        return noteDao.getNotesForBook(bookId)
    }
    
    override suspend fun addNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }
    
    override suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }
    
    override fun getReadingStats(): Flow<Map<String, Int>> {
        val toRead = readingListDao.getCountByStatus("To Read")
        val reading = readingListDao.getCountByStatus("Reading")
        val finished = readingListDao.getCountByStatus("Finished")
        
        return combine(toRead, reading, finished) { t, r, f ->
            mapOf(
                "To Read" to t,
                "Reading" to r,
                "Finished" to f
            )
        }
    }
}
