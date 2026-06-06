package com.progweb.recipify.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.Destacados
import com.progweb.recipify.ProfileSetupActivity
import com.progweb.recipify.R
import com.progweb.recipify.addRecipe.AddRecipe
import com.progweb.recipify.databinding.FragmentProfileBinding
import com.progweb.recipify.databinding.ItemRecipeGridBinding
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.recipeDetail.RecipeDetailActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recipeAdapter: UserRecipeAdapter

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
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
        cargarRecetasUsuario()
    }

    private fun setupRecyclerView() {
        recipeAdapter = UserRecipeAdapter(
            onClick = { recipe ->
                val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", recipe)
                startActivity(intent)
            },
            onLongClick = { recipe, anchorView ->
                mostrarMenuReceta(recipe, anchorView)
            }
        )
        binding.rvUserRecipes.apply {
            adapter = recipeAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun mostrarMenuReceta(recipe: Recipe, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 1, 0, "Editar")
        popup.menu.add(0, 2, 1, "Eliminar")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val intent = Intent(requireContext(), AddRecipe::class.java)
                    intent.putExtra(AddRecipe.EXTRA_RECIPE, recipe)
                    startActivity(intent)
                    true
                }
                2 -> {
                    confirmarEliminar(recipe)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmarEliminar(recipe: Recipe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar receta")
            .setMessage("¿Seguro que quieres eliminar \"${recipe.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarReceta(recipe) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarReceta(recipe: Recipe) {
        db.collection("recipe").document(recipe.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()
                cargarRecetasUsuario()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ProfileFragment", "Error eliminando receta", e)
                Toast.makeText(requireContext(), "Error al eliminar la receta", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSetupActivity::class.java))
        }
        binding.btnProfileLogout.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val username = prefs.getString("usuario", "Usuario") ?: "Usuario"
            binding.tvProfileDisplayName.text = username
            binding.tvProfileUsername.text = "@$username"
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists() && _binding != null) {
                    val displayName = doc.getString("displayName") ?: ""
                    val username = doc.getString("username") ?: ""
                    val photoURL = doc.getString("photoURL") ?: ""
                    val bookmarks = doc.getLong("bookmarksCount") ?: 0

                    binding.tvProfileDisplayName.text = if (displayName.isNotEmpty()) displayName else currentUser.displayName ?: "Recipifyer"
                    binding.tvProfileUsername.text = if (username.isNotEmpty()) "@$username" else "@usuario"
                    binding.tvBookmarksCount.text = bookmarks.toString()

                    if (photoURL.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoURL)
                            .circleCrop()
                            .placeholder(R.drawable.input_page_01)
                            .error(R.drawable.input_page_01)
                            .into(binding.ivProfileAvatar)
                    }
                }
            }
    }

    private var recetasListener: com.google.firebase.firestore.ListenerRegistration? = null

    private fun cargarRecetasUsuario() {
        val currentUser = auth.currentUser ?: return
        recetasListener?.remove()
        recetasListener = db.collection("recipe")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || _binding == null) return@addSnapshotListener
                val recipes = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(Recipe::class.java)?.apply { id = doc.id } }
                    ?.sortedByDescending { it.id }
                    ?: emptyList()
                recipeAdapter.submitList(recipes)
                binding.tvRecipesCount.text = recipes.size.toString()
            }
    }

    override fun onDestroyView() {
        recetasListener?.remove()
        super.onDestroyView()
        _binding = null
    }

    private fun cerrarSesion() {
        auth.signOut()
        val prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val intent = Intent(requireActivity(), Destacados::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }



    private class UserRecipeAdapter(
        private val onClick: (Recipe) -> Unit,
        private val onLongClick: (Recipe, View) -> Unit
    ) : RecyclerView.Adapter<UserRecipeAdapter.ViewHolder>() {

        private var recipes = listOf<Recipe>()

        fun submitList(newList: List<Recipe>) {
            recipes = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRecipeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val recipe = recipes[position]
            Glide.with(holder.itemView.context)
                .load(recipe.imageURL)
                .placeholder(R.drawable.input_page_02)
                .into(holder.binding.ivRecipeGrid)

            holder.itemView.setOnClickListener { onClick(recipe) }
            holder.itemView.setOnLongClickListener {
                onLongClick(recipe, holder.binding.btnRecipeMenu)
                true
            }
            holder.binding.btnRecipeMenu.visibility = View.VISIBLE
            holder.binding.btnRecipeMenu.setOnClickListener {
                onLongClick(recipe, holder.binding.btnRecipeMenu)
            }
        }

        override fun getItemCount() = recipes.size

        class ViewHolder(val binding: ItemRecipeGridBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
