package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.progweb.recipify.R
import com.progweb.recipify.Destacados
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
        configurarMenu() // 👈 AQUÍ agregamos el menú
    }

    // 🔧 CONFIGURAR POPUP MENU
    private fun configurarMenu() {
        val btnSettings = binding.ivSettings // asegúrate que este ID exista en tu XML

        btnSettings.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_editar -> {
                        Toast.makeText(requireContext(), "Editar información", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.menu_compartir -> {
                        Toast.makeText(requireContext(), "Compartir perfil", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.menu_logout -> {
                        cerrarSesion()
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }
    }
    private fun cerrarSesion() {
        val prefs = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(requireActivity(), Destacados::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
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
                null      -> binding.chipGroup.clearCheck()
            }
        }
    }
    private fun configurarChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedId = checkedIds.firstOrNull()
            val categoria = when (selectedId) {
                binding.chipRapidas.id -> "rapidas"
                binding.chipPostres.id -> "postres"
                binding.chipVegano.id  -> "vegano"
                else -> null
            }
            if (viewModel.categoriaSeleccionada.value != categoria) {
                viewModel.filtrar(categoria)
            }
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