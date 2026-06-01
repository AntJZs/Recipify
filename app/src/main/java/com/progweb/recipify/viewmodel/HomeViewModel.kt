package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.network.Meal
import com.progweb.recipify.network.MealApiService
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val apiService = MealApiService.create()

    private val _firestoreRecetas = MutableLiveData<List<Recipe>>(emptyList())
    private val _apiRecetas = MutableLiveData<List<Recipe>>(emptyList())

    private val _todasLasRecetas = MutableLiveData<List<Recipe>>()

    private var alfabetIndex = 0
    private val alfabeto = "abcdefghijklmnopqrstuvwxyz".map { it.toString() }
    private var estaCargando = false

    private val _recetasFiltradas = MutableLiveData<List<Recipe>>()
    val recetasFiltradas: LiveData<List<Recipe>> = _recetasFiltradas

    private val _categoriaSeleccionada = MutableLiveData<String?>(null)
    val categoriaSeleccionada: LiveData<String?> = _categoriaSeleccionada

    private val _categorias = MutableLiveData<List<String>>()
    val categorias: LiveData<List<String>> = _categorias

    init {
        fetchRecipesFromFirestore()
        fetchInitialRecipes()
    }

    private fun fetchInitialRecipes() {
        viewModelScope.launch {
            try {
                // Intentamos con "all" como pediste, pero si no trae nada usamos "" (que es el estándar de la API)
                var response = apiService.searchMeals("all")
                if (response.meals == null || response.meals!!.isEmpty()) {
                    response = apiService.searchMeals("")
                }
                
                val recipes = response.meals?.map { it.toRecipe() } ?: emptyList()
                _apiRecetas.value = recipes
                combinarYActualizar()
                
                // Si aún hay pocas recetas, cargamos la primera letra automáticamente
                if (recipes.size < 10) {
                    cargarMasRecetas()
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Error inicial", e)
                cargarMasRecetas() // Fallback a la primera letra
            }
        }
    }

    fun cargarMasRecetas() {
        if (estaCargando || alfabetIndex >= alfabeto.size) return

        estaCargando = true
        viewModelScope.launch {
            try {
                val letra = alfabeto[alfabetIndex]
                val response = apiService.searchByLetter(letra)
                val nuevasRecetas = response.meals?.map { it.toRecipe() } ?: emptyList()
                
                val actuales = _apiRecetas.value ?: emptyList()
                // Evitar duplicados si "all" ya trajo algunas de estas
                val idsExistentes = actuales.map { it.id }.toSet()
                val filtradas = nuevasRecetas.filter { it.id !in idsExistentes }
                
                _apiRecetas.value = actuales + filtradas
                alfabetIndex++
                combinarYActualizar()
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Error cargando más letra ${alfabeto[alfabetIndex]}", e)
            } finally {
                estaCargando = false
            }
        }
    }

    private fun fetchRecipesFromFirestore() {
        db.collection("recipe")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val recipes = mutableListOf<Recipe>()
                value?.documents?.forEach { doc ->
                    try {
                        val recipe = doc.toObject(Recipe::class.java)
                        recipe?.let {
                            it.id = doc.id
                            recipes.add(it)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FIRESTORE", "Error en doc: ${doc.id}", e)
                    }
                }
                _firestoreRecetas.value = recipes
                combinarYActualizar()
            }
    }

    private fun combinarYActualizar() {
        val firestore = _firestoreRecetas.value ?: emptyList()
        val api = _apiRecetas.value ?: emptyList()
        val todas = firestore + api
        
        _todasLasRecetas.value = todas
        
        val allCategories = todas.flatMap { it.category }.toSet().toList().sorted()
        _categorias.value = allCategories

        // If the selected category no longer exists, reset it
        if (_categoriaSeleccionada.value != null && !allCategories.contains(_categoriaSeleccionada.value)) {
            _categoriaSeleccionada.value = null
        }

        aplicarFiltroActual()
    }

    fun filtrar(categoria: String?) {
        _categoriaSeleccionada.value = categoria
        aplicarFiltroActual()
    }

    private fun aplicarFiltroActual() {
        val categoria = _categoriaSeleccionada.value
        val todas = _todasLasRecetas.value ?: emptyList()

        _recetasFiltradas.value = if (categoria == null) {
            todas
        } else {
            todas.filter { it.category.contains(categoria) }
        }
    }
}
