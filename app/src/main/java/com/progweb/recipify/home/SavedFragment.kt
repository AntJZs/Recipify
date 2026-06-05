package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.progweb.recipify.databinding.FragmentSavedBinding
import com.progweb.recipify.viewmodel.HomeViewModel

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
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
        adapter = RecipeAdapter(
            onItemClick = { recipe ->
                val intent = Intent(requireContext(), com.progweb.recipify.recipeDetail.RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", recipe)
                startActivity(intent)
            }
        )
        binding.rvSavedRecipes.adapter = adapter
        binding.rvSavedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun observarViewModel() {
        viewModel.todasLasRecetas.observe(viewLifecycleOwner) { todas ->
            val guardadas = todas.filter { it.isBookmarked }
            adapter.submitList(guardadas)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
