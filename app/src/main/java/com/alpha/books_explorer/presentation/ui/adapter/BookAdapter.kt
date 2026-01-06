package com.alpha.books_explorer.presentation.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alpha.books_explorer.databinding.ItemBookBinding
import com.alpha.books_explorer.domain.model.Book

class BookAdapter(private val onItemClick: (Book) -> Unit) :
    ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            val volumeInfo = book.volumeInfo
            binding.bookTitle.text = volumeInfo.title ?: "No Title"
            binding.bookAuthor.text = volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
            
            val thumbnail = volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
            binding.bookImage.load(thumbnail) {
                crossfade(true)
                error(android.R.drawable.ic_menu_report_image) 
            }
            
            // Share button functionality
            binding.shareButton.setOnClickListener {
                val shareText = "Check out this book: ${volumeInfo.title} by ${volumeInfo.authors?.joinToString(", ")}\n" +
                        (volumeInfo.previewLink ?: "")
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                binding.root.context.startActivity(shareIntent)
            }

            binding.root.setOnClickListener {
                onItemClick(book)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}
