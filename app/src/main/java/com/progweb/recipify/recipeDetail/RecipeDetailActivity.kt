package com.progweb.recipify.recipeDetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
        // Cache first — instant, works offline
        db.collection("users").document(uid)
            .collection("bookmarks").document(recipeId)
            .get(Source.CACHE)
            .addOnSuccessListener { doc ->
                isBookmarked = doc.exists()
                updateBookmarkIcon()
            }
            .addOnFailureListener {
                // Not cached yet — try network
                db.collection("users").document(uid)
                    .collection("bookmarks").document(recipeId)
                    .get()
                    .addOnSuccessListener { doc ->
                        isBookmarked = doc.exists()
                        updateBookmarkIcon()
                    }
                    .addOnFailureListener {
                        isBookmarked = false
                        updateBookmarkIcon()
                    }
            }
    }

    private fun updateBookmarkIcon() {
        binding.fabBookmark.setImageResource(
            if (isBookmarked) R.drawable.bookmark_filled_24px else R.drawable.bookmark_24px
        )
    }

    private fun setupBookmarkListener(uid: String, recipe: Recipe) {
        binding.fabBookmark.setOnClickListener {
            val bookmarksRef = db.collection("users").document(uid)
                .collection("bookmarks").document(recipe.id)
            val userRef = db.collection("users").document(uid)

            // Optimistic update: flip state and update UI immediately.
            // Firestore writes go to local cache first, sync when online.
            isBookmarked = !isBookmarked
            updateBookmarkIcon()

            if (isBookmarked) {
                val bookmarkData = hashMapOf(
                    "id"               to recipe.id,
                    "name"             to recipe.name,
                    "imageURL"         to recipe.imageURL,
                    "category"         to recipe.category,
                    "totalTimeMinutes" to recipe.totalTimeMinutes,
                    "description"      to recipe.description,
                    "body"             to recipe.body,
                    "ingredients"      to recipe.ingredients,
                    "area"             to recipe.area,
                    "userId"           to recipe.userId,
                    "bookmarkedAt"     to com.google.firebase.Timestamp.now()
                )
                bookmarksRef.set(bookmarkData)
                userRef.update("bookmarksCount", FieldValue.increment(1))
                Toast.makeText(this, getString(R.string.recipe_saved), Toast.LENGTH_SHORT).show()
            } else {
                bookmarksRef.delete()
                userRef.update("bookmarksCount", FieldValue.increment(-1))
                Toast.makeText(this, getString(R.string.recipe_removed), Toast.LENGTH_SHORT).show()
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
