package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
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
        configurarChips()
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
        viewModel.recetasFiltradas.observe(viewLifecycleOwner) { recetas ->
            adapter.submitList(recetas)
        }

        viewModel.categoriaSeleccionada.observe(viewLifecycleOwner) { categoria ->
            when (categoria) {
                "rapidas" -> binding.chipRapidas.isChecked = true
                "postres" -> binding.chipPostres.isChecked = true
                "vegano"  -> binding.chipVegano.isChecked  = true
                else      -> binding.chipTodas.isChecked   = true
            }
        }
    }

    private fun configurarChips() {
        binding.chipTodas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.filtrar("todas")
        }
        binding.chipRapidas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.filtrar("rapidas")
        }
        binding.chipPostres.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.filtrar("postres")
        }
        binding.chipVegano.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.filtrar("vegano")
        }
    }

    private fun configurarFab() {
        binding.fabAgregarReceta.setOnClickListener {
            // Abre AddRecipe como pantalla completa
            startActivity(Intent(requireContext(), AddRecipe::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}