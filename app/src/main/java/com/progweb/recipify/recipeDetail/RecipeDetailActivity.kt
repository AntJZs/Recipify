package com.progweb.recipify.recipeDetail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityRecipeDetailBinding
import com.progweb.recipify.datamodels.Recipe

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var recipe: Recipe

    private val db = FirebaseFirestore.getInstance()
    private var bookmarkListener: ListenerRegistration? = null
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipe = intent.getSerializableExtra("RECIPE") as? Recipe ?: run {
            finish()
            return
        }

        Log.d("BOOKMARK", "Recipe ID: '${recipe.id}', name: ${recipe.name}")

        setupUI(recipe)
        listenBookmarkState()

        binding.fabBookmark.setOnClickListener {
            toggleBookmark()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI(recipe: Recipe) {
        binding.tvRecipeName.text = recipe.name
        binding.tvInstructions.text = recipe.description
        binding.chipArea.text = recipe.area
        binding.chipCategory.text = recipe.category.firstOrNull() ?: "General"

        val ingredientsText = recipe.ingredients.joinToString("\n") { "• $it" }
        binding.tvIngredients.text = if (ingredientsText.isNotEmpty()) ingredientsText
                                     else "No hay ingredientes listados"

        Glide.with(this)
            .load(recipe.imageURL)
            .into(binding.ivRecipeDetail)
    }

    private fun listenBookmarkState() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w("BOOKMARK", "Usuario no autenticado, no se puede gestionar bookmark")
            binding.fabBookmark.isEnabled = false
            return
        }

        if (recipe.id.isEmpty()) {
            Log.e("BOOKMARK", "recipe.id está vacío — no se puede crear bookmark")
            binding.fabBookmark.isEnabled = false
            return
        }

        Log.d("BOOKMARK", "Escuchando bookmark: users/$userId/bookmarks/${recipe.id}")
        binding.fabBookmark.isEnabled = false

        val ref = db.collection("users").document(userId)
            .collection("bookmarks").document(recipe.id)

        bookmarkListener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("BOOKMARK", "Error al leer bookmark: ${error.code} - ${error.message}")
                binding.fabBookmark.isEnabled = true
                return@addSnapshotListener
            }
            isBookmarked = snapshot?.exists() == true
            Log.d("BOOKMARK", "Estado actual: isBookmarked=$isBookmarked")
            binding.fabBookmark.isEnabled = true
            updateBookmarkIcon()
        }
    }

    private fun toggleBookmark() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (recipe.id.isEmpty()) return

        binding.fabBookmark.isEnabled = false

        val ref = db.collection("users").document(userId)
            .collection("bookmarks").document(recipe.id)

        if (isBookmarked) {
            ref.delete()
                .addOnSuccessListener {
                    Log.d("BOOKMARK", "Bookmark eliminado: ${recipe.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("BOOKMARK", "Error al eliminar bookmark: ${e.message}")
                    binding.fabBookmark.isEnabled = true
                    Toast.makeText(this, "Error al quitar guardado: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            ref.set(mapOf("createdAt" to FieldValue.serverTimestamp()))
                .addOnSuccessListener {
                    Log.d("BOOKMARK", "Bookmark guardado: ${recipe.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("BOOKMARK", "Error al guardar bookmark: ${e.message}")
                    binding.fabBookmark.isEnabled = true
                    Toast.makeText(this, "Error al guardar receta: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        // El snapshot listener re-habilita el FAB y actualiza el ícono cuando confirma el cambio
    }

    private fun updateBookmarkIcon() {
        if (isBookmarked) {
            binding.fabBookmark.setImageResource(R.drawable.bookmark_filled_24px)
            binding.fabBookmark.imageTintList = ColorStateList.valueOf(
                getColor(R.color.md_theme_primary)
            )
        } else {
            binding.fabBookmark.setImageResource(R.drawable.bookmark_24px)
            binding.fabBookmark.imageTintList = ColorStateList.valueOf(
                getColor(R.color.md_theme_primary)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bookmarkListener?.remove()
    }
}
