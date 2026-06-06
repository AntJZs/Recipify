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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = MealApiService.create()
    private val repository = RecipeRepository.getInstance(application)

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private var searchJob: Job? = null

    init {
        repository.startFirestoreSync()
    }

    fun searchRecipes(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isOffline.value = false
            return
        }

        _isOffline.value = false

        searchJob = viewModelScope.launch {
            // Resultados locales (recetas de usuarios) de forma inmediata
            val localResults = repository.firestoreRecipes.first()
                .filter { it.name.contains(query, ignoreCase = true) }
            _searchResults.value = localResults

            // Debounce antes de llamar a la API
            delay(500)
            _isLoading.value = true

            try {
                val response = apiService.searchMeals(query)
                val apiResults = response.meals?.map { it.toRecipe() } ?: emptyList()

                // Combinar: locales primero, luego API sin duplicados
                val localIds = localResults.map { it.id }.toSet()
                _searchResults.value = localResults + apiResults.filter { it.id !in localIds }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _isOffline.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
