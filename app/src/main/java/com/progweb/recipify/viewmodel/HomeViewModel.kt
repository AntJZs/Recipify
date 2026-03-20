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

    private val _categoriaSeleccionada = MutableLiveData("todas")
    val categoriaSeleccionada: LiveData<String> = _categoriaSeleccionada

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
                value?.documents?.forEach { doc ->
                    val recipe = doc.toObject(Recipe::class.java)
                    recipe?.let {
                        it.id = doc.id
                        recipes.add(it)
                    }
                }
                _todasLasRecetas.value = recipes
                filtrar(_categoriaSeleccionada.value ?: "todas")
            }
    }

    fun filtrar(categoria: String) {
        _categoriaSeleccionada.value = categoria
        val all = _todasLasRecetas.value ?: emptyList()
        _recetasFiltradas.value = if (categoria == "todas") {
            all
        } else {
            all.filter { it.category.contains(categoria) }
        }
    }
}
