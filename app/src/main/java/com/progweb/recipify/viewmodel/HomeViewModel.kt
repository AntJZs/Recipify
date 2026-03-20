package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progweb.recipify.datamodels.Recipe

class HomeViewModel : ViewModel() {

    private val _todasLasRecetas = MutableLiveData<List<Recipe>>()

    private val _recetasFiltradas = MutableLiveData<List<Recipe>>()
    val recetasFiltradas: LiveData<List<Recipe>> = _recetasFiltradas

    private val _categoriaSeleccionada = MutableLiveData<String?>(null)
    val categoriaSeleccionada: LiveData<String?> = _categoriaSeleccionada

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        // TODO: reemplazar con Firebase cuando esté lista la conexión
        val recetas = listOf(
            Recipe(name = "Omelette Francés", totalTimeMinutes = 7,  category = listOf("rapidas"), id = "1"),
            Recipe(name = "Sandwiches",       totalTimeMinutes = 15, category = listOf("rapidas"), id = "2"),
            Recipe(name = "Tacos con carne",  totalTimeMinutes = 15, category = listOf("todas"),   id = "3"),
            Recipe(name = "Tiramisú",         totalTimeMinutes = 30, category = listOf("postres"), id = "4"),
            Recipe(name = "Ensalada Verde",   totalTimeMinutes = 10, category = listOf("vegano"),  id = "5"),
            Recipe(name = "Hamburguesa",      totalTimeMinutes = 20, category = listOf("todas"),   id = "6")
        )
        _todasLasRecetas.value = recetas
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

    fun agregarReceta(recipe: Recipe) {
        val listaActual = _todasLasRecetas.value?.toMutableList() ?: mutableListOf()
        listaActual.add(recipe)
        _todasLasRecetas.value = listaActual
        aplicarFiltroActual()
    }
}
