package com.progweb.recipify.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddRecipeViewModel : ViewModel() {

    data class UiState(
        val guardando: Boolean = false,
        val success: Boolean = false,
        val errorNombre: String? = null,
        val errorTiempo: String? = null,
        val errorIngredients: String? = null,
        val errorBody: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun saveRecipe(
        nombre: String,
        tiempo: String,
        categorias: String,
        ingredients: String,
        cuerpo: String
    ) {
        if (_uiState.value.guardando) return

        var hasError = false
        var errorNombre: String? = null
        var errorTiempo: String? = null
        var errorIngredients: String? = null
        var errorBody: String? = null

        if (nombre.trim().isEmpty()) {
            errorNombre = "Ingresa el nombre del plato"
            hasError = true
        }

        if (tiempo.trim().isEmpty()) {
            errorTiempo = "Ingresa el tiempo de preparación"
            hasError = true
        } else if (tiempo.trim().toIntOrNull() == null) {
            errorTiempo = "Ingresa un número válido"
            hasError = true
        }

        if (ingredients.trim().isEmpty()) {
            errorIngredients = "Ingresa al menos un ingrediente"
            hasError = true
        }

        if (cuerpo.trim().isEmpty()) {
            errorBody = "Ingresa los pasos de la receta"
            hasError = true
        }

        if (hasError) {
            _uiState.value = UiState(
                errorNombre = errorNombre,
                errorTiempo = errorTiempo,
                errorIngredients = errorIngredients,
                errorBody = errorBody
            )
            return
        }

        _uiState.value = UiState(guardando = true, success = true)
    }

    fun resetAfterFailure() {
        _uiState.value = _uiState.value.copy(guardando = false, success = false)
    }
}
