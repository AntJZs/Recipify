package com.progweb.recipify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.progweb.recipify.data.repository.RecipeRepository
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.network.MealApiService
import com.progweb.recipify.util.NetworkUtils
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository.getInstance(application)
    private val apiService = MealApiService.create()

    private val _firestoreRecetas = MutableLiveData<List<Recipe>>(emptyList())
    private val _apiRecetas = MutableLiveData<List<Recipe>>(emptyList())

    private val _todasLasRecetas = MutableLiveData<List<Recipe>>()
    val todasLasRecetas: LiveData<List<Recipe>> = _todasLasRecetas

    private var alfabetIndex = 0
    private val alfabeto = "abcdefghijklmnopqrstuvwxyz".map { it.toString() }
    private var estaCargando = false

    private val _recetasFiltradas = MutableLiveData<List<Recipe>>()
    val recetasFiltradas: LiveData<List<Recipe>> = _recetasFiltradas

    private val _categoriaSeleccionada = MutableLiveData<String?>(null)
    val categoriaSeleccionada: LiveData<String?> = _categoriaSeleccionada

    private val _categorias = MutableLiveData<List<String>>()
    val categorias: LiveData<List<String>> = _categorias

    private val _bookmarkedRecipeIds = MutableLiveData<Set<String>>(emptySet())
    val bookmarkedRecipeIds: LiveData<Set<String>> = _bookmarkedRecipeIds

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    init {
        repository.startFirestoreSync()
        viewModelScope.launch {
            repository.firestoreRecipes.collect { recipes ->
                _firestoreRecetas.value = recipes
                combinarYActualizar()
            }
        }
        viewModelScope.launch {
            repository.bookmarkedIds.collect { ids ->
                _bookmarkedRecipeIds.value = ids
                combinarYActualizar()
            }
        }
        fetchInitialRecipes()
    }

    private fun fetchInitialRecipes() {
        val offline = !NetworkUtils.isOnline(getApplication())
        _isOffline.postValue(offline)
        if (offline) return

        viewModelScope.launch {
            try {
                var response = apiService.searchMeals("all")
                if (response.meals == null || response.meals!!.isEmpty()) {
                    response = apiService.searchMeals("")
                }
                val recipes = response.meals?.map { it.toRecipe() } ?: emptyList()
                _apiRecetas.value = recipes
                combinarYActualizar()
                if (recipes.size < 10) cargarMasRecetas()
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Error inicial", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _isOffline.postValue(true)
                cargarMasRecetas()
            }
        }
    }

    fun cargarMasRecetas() {
        if (!NetworkUtils.isOnline(getApplication())) return
        if (estaCargando || alfabetIndex >= alfabeto.size) return
        estaCargando = true
        viewModelScope.launch {
            try {
                val letra = alfabeto[alfabetIndex]
                val response = apiService.searchByLetter(letra)
                val nuevasRecetas = response.meals?.map { it.toRecipe() } ?: emptyList()
                val actuales = _apiRecetas.value ?: emptyList()
                val idsExistentes = actuales.map { it.id }.toSet()
                val filtradas = nuevasRecetas.filter { it.id !in idsExistentes }
                _apiRecetas.value = actuales + filtradas
                alfabetIndex++
                combinarYActualizar()
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Error cargando más letra ${alfabeto[alfabetIndex]}", e)
                FirebaseCrashlytics.getInstance().recordException(e)
            } finally {
                estaCargando = false
            }
        }
    }

    private fun combinarYActualizar() {
        val firestore = _firestoreRecetas.value ?: emptyList()
        val api = _apiRecetas.value ?: emptyList()
        val bookmarks = _bookmarkedRecipeIds.value ?: emptySet()

        val todas = (firestore + api).map { recipe ->
            recipe.copy(isBookmarked = bookmarks.contains(recipe.id))
        }

        _todasLasRecetas.value = todas

        val allCategories = todas.flatMap { it.category }.toSet().toList().sorted()
        _categorias.value = allCategories

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
        _recetasFiltradas.value = if (categoria == null) todas
        else todas.filter { it.category.contains(categoria) }
    }
}
