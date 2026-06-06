package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.progweb.recipify.databinding.FragmentSavedBinding
import com.progweb.recipify.recipeDetail.RecipeDetailActivity
import com.progweb.recipify.viewmodel.SavedViewModel
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE", recipe)
            startActivity(intent)
        }
        binding.rvSavedRecipes.adapter = adapter
        binding.rvSavedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun observarViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when {
                        state.isNotLoggedIn -> {
                            binding.llEmptyState.visibility = View.VISIBLE
                            binding.rvSavedRecipes.visibility = View.GONE
                        }
                        state.isEmpty -> {
                            binding.llEmptyState.visibility = View.VISIBLE
                            binding.rvSavedRecipes.visibility = View.GONE
                        }
                        else -> {
                            binding.llEmptyState.visibility = View.GONE
                            binding.rvSavedRecipes.visibility = View.VISIBLE
                            adapter.submitList(state.recipes)
                        }
                    }

                    state.error?.let { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
