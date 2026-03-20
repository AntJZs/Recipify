package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddRecipeViewModel : ViewModel() {

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult





    data class SaveResult(
        val success: Boolean = false,
        val errorNombre: String? = null,
        val errorTiempo: String? = null
    )

    fun saveRecipe(nombre: String, tiempo: String) {
        var hasError = false
        var errorNombre: String? = null
        var errorTiempo: String? = null

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

        if (hasError) {
            _saveResult.value = SaveResult(errorNombre = errorNombre, errorTiempo = errorTiempo)
        } else {
            // TODO: Logic to save recipe (e.g., to Firebase)
            _saveResult.value = SaveResult(success = true)

        }
    }
}
