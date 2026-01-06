package com.alpha.books_explorer.presentation.ui.wishList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.alpha.books_explorer.databinding.FragmentReadingListBinding
import com.alpha.books_explorer.domain.repository.BookRepository
import com.alpha.books_explorer.domain.usecase.readingList.FetchReadingListUseCase
import com.alpha.books_explorer.presentation.ui.adapter.BookAdapter
import kotlinx.coroutines.launch

class ReadingListFragment : Fragment() {

    private var _binding: FragmentReadingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory((requireActivity().application as BooksExplorerApplication).container.bookRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BookAdapter { book ->
            val action = ReadingListFragmentDirections.actionReadingListFragmentToBookDetailFragment(book.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyText.visibility = View.GONE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }

                    if (state.books.isEmpty() && !state.isLoading) {
                         binding.emptyText.visibility = View.VISIBLE
                    } else {
                         binding.emptyText.visibility = View.GONE
                    }

                    if (state.error != null) {
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }

                    adapter.submitList(state.books)
                }
            }
        }
        
        viewModel.loadBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class WishlistViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WishlistViewModel(FetchReadingListUseCase(repository)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
