package com.alpha.books_explorer.presentation.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.alpha.books_explorer.BooksExplorerApplication
import com.alpha.books_explorer.R
import com.alpha.books_explorer.databinding.FragmentBookDetailBinding
import com.alpha.books_explorer.domain.model.Book
import com.alpha.books_explorer.domain.repository.BookRepository
import com.alpha.books_explorer.domain.usecase.FavList.AddIntoFavListUseCase
import com.alpha.books_explorer.domain.usecase.FavList.IsBookPresentInFavListUserCase
import com.alpha.books_explorer.domain.usecase.FavList.RemoveFromFavListUseCase
import com.alpha.books_explorer.domain.usecase.GetBookByIdUserCase
import com.alpha.books_explorer.domain.usecase.notes.AddNoteUseCase
import com.alpha.books_explorer.domain.usecase.notes.DeleteNoteUseCase
import com.alpha.books_explorer.domain.usecase.notes.GetNotesForBookUseCase
import com.alpha.books_explorer.domain.usecase.readingList.AddIntoReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.GetBookFromReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.IsBookPresentInReadingListUseCase
import com.alpha.books_explorer.domain.usecase.readingList.RemoveFromReadingListUseCase
import com.alpha.books_explorer.presentation.ui.adapter.NoteAdapter
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()

    private val viewModel: BookDetailViewModel by viewModels {
        BookDetailViewModelFactory((requireActivity().application as BooksExplorerApplication).container.bookRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = args.bookId
        viewModel.fetchBookById(bookId)
        
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // Share Button
        binding.shareButton.setOnClickListener {
            viewModel.bookState.value.book?.let { book ->
                val volumeInfo = book.volumeInfo
                val shareText = "Check out this book: ${volumeInfo.title} by ${volumeInfo.authors?.joinToString(", ")}\n" +
                        (volumeInfo.previewLink ?: "")
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }
        
        // Notes Adapter
        val noteAdapter = NoteAdapter { note ->
            viewModel.deleteNote(note)
        }
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notesRecyclerView.adapter = noteAdapter

        // Setup Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reading_status_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.statusSpinner.adapter = adapter
        }
        
        binding.addNoteButton.setOnClickListener {
            val content = binding.noteInput.text.toString()
            if (content.isNotBlank()) {
                viewModel.addNote(bookId, content)
                binding.noteInput.text?.clear()
            }
        }
        
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                viewModel.bookState.value.book?.let { book ->
                    viewModel.updateRating(book, rating)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.bookState.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar.visibility = View.VISIBLE
                        } else {
                            binding.progressBar.visibility = View.GONE
                        }

                        if (state.error != null) {
                            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                        }

                        state.book?.let { book ->
                            bindBook(book)
                        }
                    }
                }
                
                launch {
                    viewModel.checkReadinglistItem.collect { isPresent ->
                        if (isPresent) {
                            binding.readingListButton.text = "Remove from Reading List"
                            binding.readingListButton.setOnClickListener {
                                viewModel.bookState.value.book?.let { book ->
                                    viewModel.removeFromReadingList(book)
                                }
                            }
                            binding.userFieldsContainer.visibility = View.VISIBLE
                        } else {
                            binding.readingListButton.text = "Add to Reading List"
                            binding.readingListButton.setOnClickListener {
                                viewModel.bookState.value.book?.let { book ->
                                    viewModel.addToReadinglist(book)
                                }
                            }
                            binding.userFieldsContainer.visibility = View.GONE
                        }
                    }
                }
                
                launch {
                    viewModel.notes.collect { notes ->
                        noteAdapter.submitList(notes)
                    }
                }
            }
        }
    }

    private fun bindBook(book: Book) {
        val volumeInfo = book.volumeInfo
        binding.bookTitle.text = volumeInfo.title
        binding.bookAuthor.text = volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
        
        val description = volumeInfo.description ?: "No description available."
        binding.bookDescription.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_COMPACT)
        
        val thumbnail = volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
        binding.bookImage.load(thumbnail) {
            crossfade(true)
        }
        
        if (!volumeInfo.previewLink.isNullOrEmpty()) {
            binding.previewButton.visibility = View.VISIBLE
            binding.previewButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(volumeInfo.previewLink))
                startActivity(intent)
            }
        } else {
            binding.previewButton.visibility = View.GONE
        }

        book.readingStatus?.let { status ->
            val options = resources.getStringArray(R.array.reading_status_options)
            val index = options.indexOf(status)
            if (index >= 0) binding.statusSpinner.setSelection(index)
        }
        book.rating?.let { binding.ratingBar.rating = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class BookDetailViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookDetailViewModel(
                GetBookByIdUserCase(repository),
                GetBookFromReadingListUseCase(repository),
                AddIntoFavListUseCase(repository),
                IsBookPresentInFavListUserCase(repository),
                RemoveFromFavListUseCase(repository),
                AddIntoReadingListUseCase(repository),
                IsBookPresentInReadingListUseCase(repository),
                RemoveFromReadingListUseCase(repository),
                GetNotesForBookUseCase(repository),
                AddNoteUseCase(repository),
                DeleteNoteUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
