package com.alpha.books_explorer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpha.books_explorer.data.local.entities.ReadingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingListDao {
    @Query("SELECT * FROM readingList")
    suspend fun getReadingListBooks(): List<ReadingListEntity>
    
    // For counting stats
    @Query("SELECT COUNT(*) FROM readingList WHERE readingStatus = :status")
    fun getCountByStatus(status: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM readingList")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM readingList WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): ReadingListEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntoReadingList(book: ReadingListEntity)

    @Delete
    suspend fun deleteFromReadingList(book: ReadingListEntity)

    @Query("SELECT id FROM readingList WHERE id = :id LIMIT 1")
    suspend fun isBookPresentInReadingList(id: String): String?
}
