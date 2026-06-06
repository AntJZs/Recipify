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
import com.google.android.material.chip.Chip
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
        configurarFab()
        observarViewModel()
        configurarMenu() // 👈 AQUÍ agregamos el menú
        cargarAvatarUsuario()
    }

    private fun cargarAvatarUsuario() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val photoURL = doc.getString("photoURL")
                        if (!photoURL.isNullOrEmpty() && _binding != null) {
                            com.bumptech.glide.Glide.with(this)
                                .load(photoURL)
                                .circleCrop()
                                .placeholder(R.drawable.input_page_01)
                                .error(R.drawable.input_page_01)
                                .into(binding.ivAvatar)
                        }
                    }
                }
        }
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
        // Sign out from Firebase Auth
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        val prefs = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(requireActivity(), Destacados::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
    }
    private fun configurarRecyclerView() {
        adapter = RecipeAdapter(
            onItemClick = { recipe ->
                val intent = Intent(requireContext(), com.progweb.recipify.recipeDetail.RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", recipe)
                startActivity(intent)
            }
        )
        binding.rvRecetas.adapter = adapter
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecetas.layoutManager = layoutManager

        // Importante: Como el RecyclerView está dentro de un NestedScrollView,
        // el scroll lo maneja el contenedor padre.
        binding.nsvHome.setOnScrollChangeListener { v: androidx.core.widget.NestedScrollView, _, scrollY, _, _ ->
            // Comprobar si hemos llegado al final del NestedScrollView
            val lastChild = v.getChildAt(v.childCount - 1)
            if (lastChild != null) {
                // Si faltan menos de 200px para el final, cargamos más
                if (scrollY >= (lastChild.measuredHeight - v.measuredHeight - 200)) {
                    viewModel.cargarMasRecetas()
                }
            }
        }
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