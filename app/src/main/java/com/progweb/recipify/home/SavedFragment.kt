package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.databinding.FragmentSavedBinding
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.recipeDetail.RecipeDetailActivity

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
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
        escucharFavoritos()
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

    private fun escucharFavoritos() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvSavedRecipes.visibility = View.GONE
            return
        }

        db.collection("users").document(currentUser.uid)
            .collection("bookmarks")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    android.util.Log.e("SAVED_FRAGMENT", "Error al escuchar favoritos", error)
                    return@addSnapshotListener
                }

                if (_binding == null) return@addSnapshotListener

                val bookmarkedRecipes = mutableListOf<Recipe>()
                value?.documents?.forEach { doc ->
                    try {
                        val recipe = doc.toObject(Recipe::class.java)
                        recipe?.let {
                            it.id = doc.id
                            bookmarkedRecipes.add(it)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SAVED_FRAGMENT", "Error al parsear receta ${doc.id}", e)
                    }
                }

                if (bookmarkedRecipes.isEmpty()) {
                    binding.llEmptyState.visibility = View.VISIBLE
                    binding.rvSavedRecipes.visibility = View.GONE
                } else {
                    binding.llEmptyState.visibility = View.GONE
                    binding.rvSavedRecipes.visibility = View.VISIBLE
                    adapter.submitList(bookmarkedRecipes)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
