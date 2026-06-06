package com.progweb.recipify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.data.repository.RecipeRepository
import com.progweb.recipify.datamodels.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = RecipeRepository.getInstance(application)

    data class UiState(
        val displayName: String = "",
        val username: String = "",
        val bio: String = "",
        val photoURL: String = "",
        val bookmarksCount: Long = 0,
        val recipesCount: Int = 0,
        val recipes: List<Recipe> = emptyList(),
        val isLoading: Boolean = false,
        val loggedOut: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadProfile() {
        val currentUser = auth.currentUser ?: run {
            _uiState.value = UiState(loggedOut = true)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(currentUser.uid).get().await()

                val displayName: String
                val username: String
                val photoURL: String

                val bio: String
                if (doc.exists()) {
                    displayName = doc.getString("displayName") ?: currentUser.displayName ?: "Recipifyer"
                    username = doc.getString("username") ?: ""
                    photoURL = doc.getString("photoURL") ?: ""
                    bio = doc.getString("bio") ?: ""
                } else {
                    displayName = currentUser.displayName ?: "Recipifyer"
                    username = ""
                    photoURL = currentUser.photoUrl?.toString() ?: ""
                    bio = ""
                }

                val bookmarksCount = repository.bookmarkedIds.first().size.toLong()

                val recipesDocs = firestore.collection("recipe")
                    .whereEqualTo("userId", currentUser.uid)
                    .get().await()

                val recipes = recipesDocs.documents
                    .mapNotNull { d -> d.toObject(Recipe::class.java)?.apply { id = d.id } }
                    .sortedByDescending { it.id }

                _uiState.value = UiState(
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    photoURL = photoURL,
                    bookmarksCount = bookmarksCount,
                    recipesCount = recipes.size,
                    recipes = recipes,
                    isLoading = false
                )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil"
                )
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipeId)
                loadProfile()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(error = "Error al eliminar la receta")
            }
        }
    }

    fun logOut() {
        auth.signOut()
        _uiState.value = UiState(loggedOut = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
