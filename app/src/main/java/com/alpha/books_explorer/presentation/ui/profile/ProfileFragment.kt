package com.alpha.books_explorer.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alpha.books_explorer.BooksExplorerApplication
import com.alpha.books_explorer.R
import com.alpha.books_explorer.databinding.FragmentProfileBinding
import com.alpha.books_explorer.domain.model.ThemeMode
import com.alpha.books_explorer.domain.repository.BookRepository
import com.alpha.books_explorer.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(
            (requireActivity().application as BooksExplorerApplication).container.bookRepository,
            (requireActivity().application as BooksExplorerApplication).container.settingsRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.editNameButton.setOnClickListener {
            showEditNameDialog()
        }
        
        binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = when (checkedId) {
                    R.id.themeLight -> ThemeMode.LIGHT
                    R.id.themeDark -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                viewModel.setThemeMode(mode)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userName.collect { name ->
                        binding.userName.text = name
                    }
                }
                launch {
                    viewModel.readingListCount.collect { count ->
                        binding.readingListCount.text = count.toString()
                    }
                }
                launch {
                    viewModel.readingStats.collect { stats ->
                        binding.toReadCount.text = (stats["To Read"] ?: 0).toString()
                        binding.readingCount.text = (stats["Reading"] ?: 0).toString()
                        binding.finishedCount.text = (stats["Finished"] ?: 0).toString()
                    }
                }
                launch {
                    viewModel.themeMode.collect { mode ->
                        val checkedId = when (mode) {
                            ThemeMode.LIGHT -> R.id.themeLight
                            ThemeMode.DARK -> R.id.themeDark
                            ThemeMode.SYSTEM -> R.id.themeSystem
                        }
                        if (binding.themeToggleGroup.checkedButtonId != checkedId) {
                            binding.themeToggleGroup.check(checkedId)
                        }
                    }
                }
            }
        }
    }
    
    private fun showEditNameDialog() {
        val input = EditText(requireContext())
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) 
        params.rightMargin = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        input.layoutParams = params
        container.addView(input)
        
        // Pre-fill
        val currentName = binding.userName.text.toString()
        input.setText(currentName)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Name")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    val parts = newName.split(" ", limit = 2)
                    val firstName = parts.getOrElse(0) { "" }
                    val lastName = parts.getOrElse(1) { "" }
                    viewModel.updateName(firstName, lastName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProfileViewModelFactory(
    private val bookRepository: BookRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(bookRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
