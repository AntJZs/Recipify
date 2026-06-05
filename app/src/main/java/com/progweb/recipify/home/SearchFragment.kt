package com.progweb.recipify.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.progweb.recipify.databinding.FragmentSearchBinding
import com.progweb.recipify.viewmodel.HomeViewModel
import com.progweb.recipify.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: RecipeAdapter

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

        configurarRecyclerView()
        setupSearchListener()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = RecipeAdapter(
            onItemClick = { recipe ->
                val intent = android.content.Intent(requireContext(), com.progweb.recipify.recipeDetail.RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", recipe)
                startActivity(intent)
            }
        )
        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchRecipes(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observarViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            homeViewModel.recetasFiltradas.observe(viewLifecycleOwner) { todas ->
                val searchIds = searchResults.map { it.id }.toSet()
                val updatedResults = todas.filter { it.id in searchIds }
                adapter.submitList(updatedResults)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
