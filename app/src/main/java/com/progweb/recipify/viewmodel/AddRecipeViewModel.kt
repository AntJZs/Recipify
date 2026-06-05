package com.progweb.recipify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddRecipeViewModel : ViewModel() {

    // UiState agrupa TODO el estado de la pantalla en un solo objeto
    data class UiState(
        val guardando: Boolean = false,       // controla el doble clic
        val success: Boolean = false,
        val errorNombre: String? = null,
        val errorTiempo: String? = null,
        val errorBody: String? = null
    )

    // MutableStateFlow privado — solo el ViewModel puede modificarlo
    private val _uiState = MutableStateFlow(UiState())

    // StateFlow público — el Fragment solo puede leerlo
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun saveRecipe(
        nombre: String,
        tiempo: String,
        categorias: String,
        cuerpo: String
    ) {

        // Evita doble clic — si ya está guardando, ignora la llamada
        if (_uiState.value.guardando) return

        var hasError = false
        var errorNombre: String? = null
        var errorTiempo: String? = null
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

        if (cuerpo.trim().isEmpty()) {
            errorBody = "Ingresa el cuerpo de la receta"
            hasError = true
        }

        if (hasError) {
            _uiState.value = UiState(
                errorNombre = errorNombre,
                errorTiempo = errorTiempo,
                errorBody = errorBody
            )
            return
        }

        // Sin errores — activa guardando para deshabilitar el botón
        viewModelScope.launch {
            _uiState.value = UiState(guardando = true)

            // TODO: aquí irá la llamada real a Firebase (se hará en Activity)
            // Simulación por ahora
            _uiState.value = UiState(success = true, guardando = false)
        }
    }
}
