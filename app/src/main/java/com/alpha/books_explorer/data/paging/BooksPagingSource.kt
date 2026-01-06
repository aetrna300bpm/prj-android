package com.alpha.books_explorer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alpha.books_explorer.data.remote.BookApiService
import com.alpha.books_explorer.domain.model.Book

class BooksPagingSource(
    private val api: BookApiService,
    private val query: String,
    private val filter: String? = null,
    private val orderBy: String? = null,
    private val printType: String? = null
) : PagingSource<Int, Book>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val position = params.key ?: 0
        return try {
            val response = api.searchBooks(
                query = query,
                startIndex = position,
                maxResults = params.loadSize,
                filter = filter,
                orderBy = orderBy,
                printType = printType
            )

            val books = response.items ?: emptyList()

            LoadResult.Page(
                data = books,
                prevKey = if (position == 0) null else position - params.loadSize,
                nextKey = if (books.isEmpty()) null else position + params.loadSize,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition
    }
}
