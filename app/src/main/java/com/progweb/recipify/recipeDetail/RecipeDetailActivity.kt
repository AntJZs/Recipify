package com.progweb.recipify.recipeDetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityRecipeDetailBinding
import com.progweb.recipify.datamodels.Recipe

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipe = intent.getSerializableExtra("RECIPE") as? Recipe
        
        recipe?.let {
            setupUI(it)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                checkBookmarkStatus(currentUser.uid, it.id)
                setupBookmarkListener(currentUser.uid, it)
            } else {
                binding.fabBookmark.visibility = android.view.View.GONE
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun checkBookmarkStatus(uid: String, recipeId: String) {
        db.collection("users").document(uid)
            .collection("bookmarks").document(recipeId)
            .get()
            .addOnSuccessListener { doc ->
                isBookmarked = doc != null && doc.exists()
                updateBookmarkIcon()
            }
    }

    private fun updateBookmarkIcon() {
        if (isBookmarked) {
            binding.fabBookmark.setImageResource(R.drawable.bookmark_filled_24px)
        } else {
            binding.fabBookmark.setImageResource(R.drawable.bookmark_24px)
        }
    }

    private fun setupBookmarkListener(uid: String, recipe: Recipe) {
        binding.fabBookmark.setOnClickListener {
            val bookmarksRef = db.collection("users").document(uid).collection("bookmarks").document(recipe.id)
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->
                val bookmarkDoc = transaction.get(bookmarksRef)
                val userDoc = transaction.get(userRef)

                val currentBookmarkCount = userDoc.getLong("bookmarksCount") ?: 0

                if (bookmarkDoc.exists()) {
                    transaction.delete(bookmarksRef)
                    transaction.update(userRef, "bookmarksCount", maxOf(0, currentBookmarkCount - 1))
                    false
                } else {
                    val bookmarkData = hashMapOf(
                        "id" to recipe.id,
                        "name" to recipe.name,
                        "imageURL" to recipe.imageURL,
                        "category" to recipe.category,
                        "totalTimeMinutes" to recipe.totalTimeMinutes,
                        "description" to recipe.description,
                        "body" to recipe.body,
                        "ingredients" to recipe.ingredients,
                        "area" to recipe.area,
                        "userId" to recipe.userId,
                        "bookmarkedAt" to com.google.firebase.Timestamp.now()
                    )
                    transaction.set(bookmarksRef, bookmarkData)
                    transaction.update(userRef, "bookmarksCount", currentBookmarkCount + 1)
                    true
                }
            }.addOnSuccessListener { newBookmarkState ->
                isBookmarked = newBookmarkState
                updateBookmarkIcon()
                val message = if (isBookmarked) "Receta guardada" else "Receta eliminada de guardados"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI(recipe: Recipe) {
        binding.tvRecipeName.text = recipe.name
        binding.tvInstructions.text = recipe.getDisplayDescription()
        binding.chipArea.text = recipe.area
        binding.chipCategory.text = recipe.category.firstOrNull() ?: "General"
        
        val ingredientsText = recipe.getFormattedIngredients().joinToString("\n") { "• $it" }
        binding.tvIngredients.text = if (ingredientsText.isNotEmpty()) ingredientsText else "No hay ingredientes listados"

        Glide.with(this)
            .load(recipe.imageURL)
            .into(binding.ivRecipeDetail)
    }
}
