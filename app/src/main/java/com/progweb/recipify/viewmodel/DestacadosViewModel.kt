package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.network.MealApiService
import kotlinx.coroutines.launch

class DestacadosViewModel : ViewModel() {

    private val apiService = MealApiService.create()

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> = _navigateToRegister

    private val _recetas = MutableLiveData<List<Recipe>>(emptyList())
    val recetas: LiveData<List<Recipe>> = _recetas

    init {
        cargarRecetas()
    }

    private fun cargarRecetas() {
        viewModelScope.launch {
            try {
                val letras = listOf("c", "b", "a")
                val todas = mutableListOf<Recipe>()
                for (letra in letras) {
                    val resp = apiService.searchByLetter(letra)
                    val recetas = resp.meals?.map { it.toRecipe() } ?: emptyList()
                    todas.addAll(recetas)
                    if (todas.size >= 20) break
                }
                _recetas.value = todas.take(20)
            } catch (e: Exception) {
                android.util.Log.e("DestacadosVM", "Error cargando recetas", e)
            }
        }
    }

    fun onLoginClicked() {
        _navigateToLogin.value = true
    }

    fun onRegisterClicked() {
        _navigateToRegister.value = true
    }

    fun onNavigationDone() {
        _navigateToLogin.value = false
        _navigateToRegister.value = false
    }
}
