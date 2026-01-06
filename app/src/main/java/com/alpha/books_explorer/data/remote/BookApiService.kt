package com.alpha.books_explorer.data.remote

import com.alpha.books_explorer.data.remote.dto.BookSearchResponse
import com.alpha.books_explorer.domain.model.Book
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int,
        @Query("maxResults") maxResults: Int,
        @Query("filter") filter: String? = null, // partial, full, free-ebooks, paid-ebooks, ebooks
        @Query("orderBy") orderBy: String? = null, // relevance, newest
        @Query("printType") printType: String? = null // all, books, magazines
    ): BookSearchResponse

    @GET("volumes/{bookId}")
    suspend fun getBookById(
        @Path("bookId") bookId: String,
        @Query("key") key: String = "",
    ): Book
}
