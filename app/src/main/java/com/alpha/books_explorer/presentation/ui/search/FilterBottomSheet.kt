package com.alpha.books_explorer.presentation.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpha.books_explorer.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheet(
    private val currentFilter: String?,
    private val currentOrderBy: String?,
    private val currentPrintType: String?,
    private val onApply: (String?, String?, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial state
        when (currentFilter) {
            "free-ebooks" -> binding.filterFree.isChecked = true
            "paid-ebooks" -> binding.filterPaid.isChecked = true
            else -> binding.filterAll.isChecked = true
        }

        when (currentOrderBy) {
            "newest" -> binding.sortNewest.isChecked = true
            else -> binding.sortRelevance.isChecked = true
        }
        
        when (currentPrintType) {
            "books" -> binding.printBooks.isChecked = true
            "magazines" -> binding.printMagazines.isChecked = true
            else -> binding.printAll.isChecked = true
        }

        binding.applyFiltersButton.setOnClickListener {
            val filter = when {
                binding.filterFree.isChecked -> "free-ebooks"
                binding.filterPaid.isChecked -> "paid-ebooks"
                else -> null
            }

            val orderBy = when {
                binding.sortNewest.isChecked -> "newest"
                else -> "relevance"
            }
            
            val printType = when {
                binding.printBooks.isChecked -> "books"
                binding.printMagazines.isChecked -> "magazines"
                else -> "all"
            }

            onApply(filter, orderBy, printType)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
