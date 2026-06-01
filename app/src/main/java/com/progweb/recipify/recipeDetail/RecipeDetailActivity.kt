package com.progweb.recipify.recipeDetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.progweb.recipify.databinding.ActivityRecipeDetailBinding
import com.progweb.recipify.datamodels.Recipe

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipe = intent.getSerializableExtra("RECIPE") as? Recipe
        
        recipe?.let {
            setupUI(it)
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
        binding.tvIngredients.text = if (ingredientsText.isNotEmpty()) ingredientsText else "No hay ingredientes listados"

        Glide.with(this)
            .load(recipe.imageURL)
            .into(binding.ivRecipeDetail)
    }
}
