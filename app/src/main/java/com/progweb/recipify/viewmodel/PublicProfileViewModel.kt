package com.progweb.recipify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.datamodels.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PublicProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()

    data class UiState(
        val displayName: String = "",
        val username: String = "",
        val bio: String = "",
        val photoURL: String = "",
        val recipesCount: Int = 0,
        val recipes: List<Recipe> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                val displayName = doc.getString("displayName") ?: "Recipifyer"
                val username = doc.getString("username") ?: ""
                val bio = doc.getString("bio") ?: ""
                val photoURL = doc.getString("photoURL") ?: ""

                val recipesDocs = firestore.collection("recipe")
                    .whereEqualTo("userId", userId)
                    .get().await()

                val recipes = recipesDocs.documents
                    .mapNotNull { d -> d.toObject(Recipe::class.java)?.apply { id = d.id } }
                    .sortedByDescending { it.id }

                _uiState.value = UiState(
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    photoURL = photoURL,
                    recipesCount = recipes.size,
                    recipes = recipes,
                    isLoading = false
                )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo cargar el perfil"
                )
            }
        }
    }
}
