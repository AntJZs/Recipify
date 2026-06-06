package com.progweb.recipify.recipeDetail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityRecipeDetailBinding
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.publicProfile.PublicProfileActivity
import com.progweb.recipify.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val viewModel: RecipeDetailViewModel by viewModels()
    private var authorUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val recipe = intent.getSerializableExtra("RECIPE") as? Recipe
        if (recipe == null) { finish(); return }

        viewModel.init(recipe)
        setupStaticUI(recipe)
        setupToolbar()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.menu_recipe_detail)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                showDeleteConfirmation()
                true
            } else false
        }
    }

    private fun observeViewModel() {
        binding.fabBookmark.setOnClickListener { viewModel.toggleBookmark() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    binding.fabBookmark.visibility =
                        if (state.isLoggedIn) View.VISIBLE else View.GONE

                    binding.fabBookmark.setImageResource(
                        if (state.isBookmarked) R.drawable.bookmark_filled_24px
                        else R.drawable.bookmark_24px
                    )
                    binding.fabBookmark.isEnabled = !state.bookmarkLoading

                    if (state.authorName.isNotEmpty()) {
                        binding.tvAuthorName.text = "por ${state.authorName}"
                    }

                    binding.toolbar.menu.findItem(R.id.action_delete)?.isVisible = state.isOwner

                    if (state.deleted) {
                        Toast.makeText(
                            this@RecipeDetailActivity,
                            "Receta eliminada",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return@collect
                    }

                    state.error?.let { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar receta")
            .setMessage("¿Eliminar esta receta permanentemente? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteRecipe() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupStaticUI(recipe: Recipe) {
        authorUserId = recipe.userId
        binding.tvRecipeName.text = recipe.name
        binding.tvAuthorName.text = "por ..."

        if (recipe.userId.isNotEmpty()) {
            binding.tvAuthorName.setOnClickListener {
                startActivity(
                    Intent(this, PublicProfileActivity::class.java)
                        .putExtra(PublicProfileActivity.EXTRA_USER_ID, recipe.userId)
                )
            }
        }

        binding.tvInstructions.text = parseMarkdown(recipe.getDisplayDescription())
        binding.chipArea.text = recipe.area
        binding.chipCategory.text = recipe.category.firstOrNull() ?: getString(R.string.general)

        val ingredientsText = recipe.getFormattedIngredients().joinToString("\n") { "• $it" }
        binding.tvIngredients.text =
            ingredientsText.ifEmpty { getString(R.string.no_ingredients_listed) }

        Glide.with(this).load(recipe.imageURL).into(binding.ivRecipeDetail)
    }

    private fun parseMarkdown(text: String): android.text.SpannableStringBuilder {
        val ssb = android.text.SpannableStringBuilder(text)
        Regex("###\\s*(.*)").findAll(text).forEach { match ->
            ssb.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                match.range.first, match.range.last + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                android.text.style.RelativeSizeSpan(1.2f),
                match.range.first, match.range.last + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        Regex("##\\s*(.*)").findAll(text).forEach { match ->
            ssb.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                match.range.first, match.range.last + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                android.text.style.RelativeSizeSpan(1.4f),
                match.range.first, match.range.last + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ssb
    }
}
