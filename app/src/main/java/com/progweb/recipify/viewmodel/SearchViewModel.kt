package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.network.Meal
import com.progweb.recipify.network.MealApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val apiService = MealApiService.create()

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var searchJob: Job? = null

    fun searchRecipes(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(500) // Debounce
            try {
                val response = apiService.searchMeals(query)
                val recipes = response.meals?.map { it.toRecipe() } ?: emptyList()
                _searchResults.value = recipes
            } catch (e: Exception) {
                android.util.Log.e("SEARCH_ERROR", "Error searching: $query", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun Meal.toRecipe(): Recipe {
        return Recipe(
            name = strMeal,
            totalTimeMinutes = 25,
            category = listOf(strCategory),
            description = strInstructions,
            imageURL = strMealThumb,
            id = "api_$idMeal"
        )
    }
}
