package com.alpha.books_explorer

import android.app.Application
import androidx.room.Room
import com.alpha.books_explorer.data.local.FavBookDatabase
import com.alpha.books_explorer.data.remote.BookApiService
import com.alpha.books_explorer.data.repository.BookRepositoryImpl
import com.alpha.books_explorer.domain.repository.BookRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BooksExplorerApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

interface AppContainer {
    val bookRepository: BookRepository
}

class DefaultAppContainer(private val context: Application) : AppContainer {
    
    private val baseUrl = "https://www.googleapis.com/books/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: BookApiService by lazy {
        retrofit.create(BookApiService::class.java)
    }

    private val database: FavBookDatabase by lazy {
        Room.databaseBuilder(context, FavBookDatabase::class.java, "fav_books_db")
            //.fallbackToDestructiveMigration() // Useful during dev if schema changes
            .build()
    }

    override val bookRepository: BookRepository by lazy {
        BookRepositoryImpl(
            api = retrofitService,
            localDao = database.getFavBookDao(),
            readingListDao = database.getReadingListDao(),
            database = database
        )
    }
}
