package com.alpha.books_explorer.presentation.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.books_explorer.BooksExplorerApplication
import com.alpha.books_explorer.databinding.FragmentSearchBinding
import com.alpha.books_explorer.domain.repository.BookRepository
import com.alpha.books_explorer.domain.usecase.GetBooksUseCase
import com.alpha.books_explorer.presentation.ui.adapter.BookPagingAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory((requireActivity().application as BooksExplorerApplication).container.bookRepository)
    }
    
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BookPagingAdapter { book ->
            val action = SearchFragmentDirections.actionSearchFragmentToBookDetailFragment(book.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
        
        binding.searchContainer.setEndIconOnClickListener {
            showFilterBottomSheet()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchBookList.collect { state ->
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }

                    if (state.error != null) {
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }

                    state.books?.let { flow ->
                        searchJob?.cancel()
                        searchJob = launch {
                            flow.collectLatest { pagingData ->
                                adapter.submitData(pagingData)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString()
        if (query.isNotEmpty()) {
            viewModel.loadBooks(query)
        }
    }
    
    private fun showFilterBottomSheet() {
        val bottomSheet = FilterBottomSheet(
            currentFilter = viewModel.currentFilter,
            currentOrderBy = viewModel.currentOrderBy,
            currentPrintType = viewModel.currentPrintType
        ) { filter, orderBy, printType ->
            viewModel.updateFilters(filter, orderBy, printType)
        }
        bottomSheet.show(parentFragmentManager, "FilterBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SearchViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(GetBooksUseCase(repository)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
