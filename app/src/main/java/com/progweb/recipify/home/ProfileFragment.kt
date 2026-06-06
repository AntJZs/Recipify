package com.progweb.recipify.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.progweb.recipify.Destacados
import com.progweb.recipify.ProfileSetupActivity
import com.progweb.recipify.R
import com.progweb.recipify.addRecipe.AddRecipe
import com.progweb.recipify.databinding.FragmentProfileBinding
import com.progweb.recipify.databinding.ItemRecipeGridBinding
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.recipeDetail.RecipeDetailActivity
import com.progweb.recipify.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
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
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
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
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteRecipe(recipe.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSetupActivity::class.java))
        }
        binding.btnProfileLogout.setOnClickListener {
            viewModel.logOut()
        }
    }

    private fun observarViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    if (state.loggedOut) {
                        navigateToLanding()
                        return@collect
                    }

                    binding.tvProfileUsername.text =
                        if (state.username.isNotEmpty()) "@${state.username}" else "@usuario"
                    binding.tvProfileDisplayName.text = state.displayName
                    if (state.bio.isNotEmpty()) {
                        binding.tvProfileBio.text = state.bio
                        binding.tvProfileBio.visibility = View.VISIBLE
                    } else {
                        binding.tvProfileBio.visibility = View.GONE
                    }
                    binding.tvBookmarksCount.text = state.bookmarksCount.toString()
                    binding.tvRecipesCount.text = state.recipesCount.toString()

                    if (state.photoURL.isNotEmpty()) {
                        Glide.with(this@ProfileFragment)
                            .load(state.photoURL)
                            .circleCrop()
                            .placeholder(R.drawable.input_page_01)
                            .error(R.drawable.input_page_01)
                            .into(binding.ivProfileAvatar)
                    }

                    recipeAdapter.submitList(state.recipes)

                    state.error?.let { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun navigateToLanding() {
        val intent = Intent(requireActivity(), Destacados::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
