package com.progweb.recipify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.progweb.recipify.data.repository.RecipeRepository
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var recipe: Recipe? = null

    data class UiState(
        val isBookmarked: Boolean = false,
        val authorName: String = "",
        val isOwner: Boolean = false,
        val isLoggedIn: Boolean = false,
        val bookmarkLoading: Boolean = false,
        val deleted: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun init(recipe: Recipe) {
        if (this.recipe != null) return
        this.recipe = recipe

        val currentUser = auth.currentUser
        _uiState.value = UiState(
            isBookmarked = recipe.isBookmarked,
            isOwner = currentUser != null && recipe.userId == currentUser.uid,
            isLoggedIn = currentUser != null,
            isOffline = !NetworkUtils.isOnline(getApplication())
        )
        fetchAuthorName(recipe.id)
    }

    private fun fetchAuthorName(recipeId: String) {
        if (recipeId.isEmpty()) return
        firestore.collection("recipe").document(recipeId).get()
            .addOnSuccessListener { doc ->
                val author = doc.getString("authorName") ?: return@addOnSuccessListener
                _uiState.value = _uiState.value.copy(authorName = author)
            }
    }

    fun toggleBookmark() {
        val currentUser = auth.currentUser ?: return
        val currentRecipe = recipe ?: return
        if (_uiState.value.bookmarkLoading) return

        _uiState.value = _uiState.value.copy(bookmarkLoading = true)

        val bookmarksRef = firestore.collection("users").document(currentUser.uid)
            .collection("bookmarks").document(currentRecipe.id)
        val userRef = firestore.collection("users").document(currentUser.uid)

        firestore.runTransaction { transaction ->
            val bookmarkDoc = transaction.get(bookmarksRef)
            val userDoc = transaction.get(userRef)
            val currentCount = userDoc.getLong("bookmarksCount") ?: 0

            if (bookmarkDoc.exists()) {
                transaction.delete(bookmarksRef)
                transaction.set(userRef, mapOf("bookmarksCount" to maxOf(0, currentCount - 1)), SetOptions.merge())
                false
            } else {
                val data = mapOf(
                    "id"               to currentRecipe.id,
                    "name"             to currentRecipe.name,
                    "imageURL"         to currentRecipe.imageURL,
                    "category"         to currentRecipe.category,
                    "totalTimeMinutes" to currentRecipe.totalTimeMinutes,
                    "description"      to currentRecipe.description,
                    "body"             to currentRecipe.body,
                    "ingredients"      to currentRecipe.ingredients,
                    "area"             to currentRecipe.area,
                    "userId"           to currentRecipe.userId,
                    "bookmarkedAt"     to Timestamp.now()
                )
                transaction.set(bookmarksRef, data)
                transaction.set(userRef, mapOf("bookmarksCount" to currentCount + 1), SetOptions.merge())
                true
            }
        }.addOnSuccessListener { newBookmarkState ->
            _uiState.value = _uiState.value.copy(
                isBookmarked = newBookmarkState,
                bookmarkLoading = false
            )
        }.addOnFailureListener { e ->
            FirebaseCrashlytics.getInstance().recordException(e)
            _uiState.value = _uiState.value.copy(
                bookmarkLoading = false,
                error = "Error al actualizar favorito"
            )
        }
    }

    fun deleteRecipe() {
        val currentRecipe = recipe ?: return
        val currentUser = auth.currentUser ?: return
        if (currentRecipe.userId != currentUser.uid) return

        viewModelScope.launch {
            try {
                repository.deleteRecipe(currentRecipe.id)
                _uiState.value = _uiState.value.copy(deleted = true)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(error = "Error al eliminar la receta")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
