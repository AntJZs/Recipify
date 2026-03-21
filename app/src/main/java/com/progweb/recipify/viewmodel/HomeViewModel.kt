package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.datamodels.Recipe

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _todasLasRecetas = MutableLiveData<List<Recipe>>()

    private val _recetasFiltradas = MutableLiveData<List<Recipe>>()
    val recetasFiltradas: LiveData<List<Recipe>> = _recetasFiltradas

    private val _categoriaSeleccionada = MutableLiveData<String?>(null)
    val categoriaSeleccionada: LiveData<String?> = _categoriaSeleccionada

    private val _categorias = MutableLiveData<List<String>>()
    val categorias: LiveData<List<String>> = _categorias

    init {
        fetchRecipesFromFirestore()
    }

    private fun fetchRecipesFromFirestore() {
        db.collection("recipe")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val recipes = mutableListOf<Recipe>()
                val allCategories = mutableSetOf<String>()

                value?.documents?.forEach { doc ->
                    try {
                        val recipe = doc.toObject(Recipe::class.java)
                        recipe?.let {
                            it.id = doc.id
                            recipes.add(it)
                            allCategories.addAll(it.category)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FIRESTORE", "Error en doc: ${doc.id}", e)
                    }
                }
                _todasLasRecetas.value = recipes
                _categorias.value = allCategories.toList().sorted()
                
                // If the selected category no longer exists, reset it
                if (_categoriaSeleccionada.value != null && !allCategories.contains(_categoriaSeleccionada.value)) {
                    _categoriaSeleccionada.value = null
                }
                
                aplicarFiltroActual()
            }
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
