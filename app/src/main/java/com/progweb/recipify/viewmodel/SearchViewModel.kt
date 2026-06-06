package com.progweb.recipify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.progweb.recipify.datamodels.Recipe
import com.progweb.recipify.network.MealApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = MealApiService.create()

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private var searchJob: Job? = null

    fun searchRecipes(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isOffline.value = false
            return
        }

        _isOffline.value = false
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(500)
            try {
                val response = apiService.searchMeals(query)
                _searchResults.value = response.meals?.map { it.toRecipe() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("SEARCH_ERROR", "Error searching: $query", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _isOffline.value = true
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
