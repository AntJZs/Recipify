package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.progweb.recipify.R
import com.progweb.recipify.addRecipe.AddRecipe
import com.progweb.recipify.databinding.FragmentHomeBinding
import com.progweb.recipify.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarFab()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            // TODO: navegar al detalle de la receta
        }
        binding.rvRecetas.adapter = adapter
        binding.rvRecetas.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun observarViewModel() {
        // Observer for recipes
        viewModel.recetasFiltradas.observe(viewLifecycleOwner) { recetas ->
            adapter.submitList(recetas)
        }

        // Observer for dynamic categories
        viewModel.categorias.observe(viewLifecycleOwner) { categorias ->
            actualizarChips(categorias)
        }

        // Observer for selected category
        viewModel.categoriaSeleccionada.observe(viewLifecycleOwner) { selectedCategory ->
            // Update chip states if necessary
            for (i in 0 until binding.chipGroup.childCount) {
                val chip = binding.chipGroup.getChildAt(i) as Chip
                chip.isChecked = (chip.text == selectedCategory)
            }
        }
    }

    private fun actualizarChips(categorias: List<String>) {
        val currentSelected = viewModel.categoriaSeleccionada.value
        binding.chipGroup.removeAllViews()

        categorias.forEach { categoria ->
            val chip = layoutInflater.inflate(R.layout.layout_chip_filter, binding.chipGroup, false) as Chip
            chip.text = categoria
            chip.isChecked = (categoria == currentSelected)
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.filtrar(categoria)
                } else if (binding.chipGroup.checkedChipId == View.NO_ID) {
                    // If the currently checked chip was unchecked and none other is checked
                    viewModel.filtrar(null)
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun configurarFab() {
        binding.fabAgregarReceta.setOnClickListener {
            startActivity(Intent(requireContext(), AddRecipe::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}