package com.progweb.recipify.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.progweb.recipify.datamodels.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SavedViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    data class UiState(
        val recipes: List<Recipe> = emptyList(),
        val isLoading: Boolean = true,
        val isEmpty: Boolean = false,
        val isNotLoggedIn: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        startListening()
    }

    private fun startListening() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = UiState(isLoading = false, isNotLoggedIn = true)
            return
        }

        listenerRegistration = firestore
            .collection("users").document(currentUser.uid)
            .collection("bookmarks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirebaseCrashlytics.getInstance().recordException(error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar favoritos"
                    )
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.apply {
                        id = doc.id
                        isBookmarked = true
                    }
                } ?: emptyList()

                _uiState.value = UiState(
                    recipes = recipes,
                    isLoading = false,
                    isEmpty = recipes.isEmpty()
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
