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
                val message = if (isBookmarked) getString(R.string.recipe_saved) else getString(R.string.recipe_removed)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.error_message_prefix, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI(recipe: Recipe) {
        binding.tvRecipeName.text = recipe.name

        // Fetch authorName from Firestore if it exists in the object
        val authorText = if (recipe.userId.isNotEmpty()) {
            // Check if authorName is already in the object (it should be now)
            // But if it's an old recipe or we want to be sure, we could fetch it
            // For now, let's assume it's in the Recipe object or fetch it from firestore
            "por ${recipe.userId}" // Fallback
        } else {
            ""
        }
        
        // Let's try to get authorName from the document if available
        db.collection("recipe").document(recipe.id).get().addOnSuccessListener { doc ->
            val author = doc.getString("authorName") ?: "Usuario"
            binding.tvAuthorName.text = "por $author"
        }

        // Simple Markdown parsing for ## Title, ### Subtitle
        val formattedBody = parseMarkdown(recipe.getDisplayDescription())
        binding.tvInstructions.text = formattedBody

        binding.chipArea.text = recipe.area
        binding.chipCategory.text = recipe.category.firstOrNull() ?: getString(R.string.general)
        
        val ingredientsText = recipe.getFormattedIngredients().joinToString("\n") { "• $it" }
        binding.tvIngredients.text = if (ingredientsText.isNotEmpty()) ingredientsText else getString(R.string.no_ingredients_listed)

        Glide.with(this)
            .load(recipe.imageURL)
            .into(binding.ivRecipeDetail)
    }

    private fun parseMarkdown(text: String): android.text.SpannableStringBuilder {
        val ssb = android.text.SpannableStringBuilder(text)
        
        // Match ### Subtitle
        val subtitleRegex = Regex("###\\s*(.*)")
        subtitleRegex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            ssb.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(android.text.style.RelativeSizeSpan(1.2f), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        // Match ## Title
        val titleRegex = Regex("##\\s*(.*)")
        titleRegex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            ssb.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(android.text.style.RelativeSizeSpan(1.4f), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        return ssb
    }
}
