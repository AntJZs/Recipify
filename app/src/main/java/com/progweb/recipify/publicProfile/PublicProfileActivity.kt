package com.progweb.recipify.publicProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityPublicProfileBinding
import com.progweb.recipify.databinding.ItemRecipeGridBinding
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.recipeDetail.RecipeDetailActivity
import com.progweb.recipify.viewmodel.PublicProfileViewModel
import kotlinx.coroutines.launch

class PublicProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private lateinit var binding: ActivityPublicProfileBinding
    private val viewModel: PublicProfileViewModel by viewModels()
    private lateinit var recipeAdapter: PublicRecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: run { finish(); return }

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadProfile(userId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recipeAdapter = PublicRecipeAdapter { recipe ->
            startActivity(
                Intent(this, RecipeDetailActivity::class.java)
                    .putExtra("RECIPE", recipe)
            )
        }
        binding.rvPublicRecipes.apply {
            adapter = recipeAdapter
            layoutManager = GridLayoutManager(this@PublicProfileActivity, 3)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    binding.tvPublicUsername.text =
                        if (state.username.isNotEmpty()) "@${state.username}" else "@usuario"
                    binding.tvPublicDisplayName.text = state.displayName
                    binding.tvPublicRecipesCount.text = state.recipesCount.toString()

                    if (state.bio.isNotEmpty()) {
                        binding.tvPublicBio.text = state.bio
                        binding.tvPublicBio.visibility = View.VISIBLE
                    } else {
                        binding.tvPublicBio.visibility = View.GONE
                    }

                    if (state.photoURL.isNotEmpty()) {
                        Glide.with(this@PublicProfileActivity)
                            .load(state.photoURL)
                            .circleCrop()
                            .placeholder(R.drawable.input_page_01)
                            .error(R.drawable.input_page_01)
                            .into(binding.ivPublicAvatar)
                    }

                    recipeAdapter.submitList(state.recipes)

                    binding.tvEmpty.visibility =
                        if (!state.isLoading && state.recipes.isEmpty()) View.VISIBLE else View.GONE

                    state.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private class PublicRecipeAdapter(
        private val onClick: (Recipe) -> Unit
    ) : RecyclerView.Adapter<PublicRecipeAdapter.ViewHolder>() {

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
                .into(holder.binding.ivRecipeGrid)
            holder.binding.btnRecipeMenu.visibility = View.GONE
            holder.itemView.setOnClickListener { onClick(recipe) }
        }

        override fun getItemCount() = recipes.size

        class ViewHolder(val binding: ItemRecipeGridBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
