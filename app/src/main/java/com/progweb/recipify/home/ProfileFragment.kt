package com.progweb.recipify.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.progweb.recipify.Destacados
import com.progweb.recipify.ProfileSetupActivity
import com.progweb.recipify.R
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
        recipeAdapter = UserRecipeAdapter { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE", recipe)
            startActivity(intent)
        }
        binding.rvUserRecipes.apply {
            adapter = recipeAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileSetupActivity::class.java)
            startActivity(intent)
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
                            .placeholder(R.drawable.input_page_01)
                            .error(R.drawable.input_page_01)
                            .into(binding.ivProfileAvatar)
                    }
                }
            }
    }

    private fun cargarRecetasUsuario() {
        val currentUser = auth.currentUser ?: return

        db.collection("recipe")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener
                
                val recipes = result.documents.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.apply { id = doc.id }
                }
                
                recipeAdapter.submitList(recipes)
                binding.tvRecipesCount.text = recipes.size.toString()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ProfileFragment", "Error cargando recetas", e)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner class Adapter for the grid
    private class UserRecipeAdapter(private val onClick: (Recipe) -> Unit) :
        RecyclerView.Adapter<UserRecipeAdapter.ViewHolder>() {

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
        }

        override fun getItemCount() = recipes.size

        class ViewHolder(val binding: ItemRecipeGridBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
