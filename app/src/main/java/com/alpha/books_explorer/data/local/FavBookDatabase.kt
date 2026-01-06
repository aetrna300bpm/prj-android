package com.alpha.books_explorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alpha.books_explorer.data.local.converters.Converters
import com.alpha.books_explorer.data.local.dao.FavBookDao
import com.alpha.books_explorer.data.local.dao.NoteDao
import com.alpha.books_explorer.data.local.dao.ReadingListDao
import com.alpha.books_explorer.data.local.entities.BookEntity
import com.alpha.books_explorer.data.local.entities.NoteEntity
import com.alpha.books_explorer.data.local.entities.ReadingListEntity

@Database(entities = [BookEntity::class, ReadingListEntity::class, NoteEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class FavBookDatabase : RoomDatabase() {
    abstract fun getFavBookDao(): FavBookDao

    abstract fun getReadingListDao(): ReadingListDao
    
    abstract fun getNoteDao(): NoteDao
}
