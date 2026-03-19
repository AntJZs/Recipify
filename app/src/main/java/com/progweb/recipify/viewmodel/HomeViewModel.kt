package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progweb.recipify.datamodels.Recipe

class HomeViewModel : ViewModel() {

    private val _todasLasRecetas = MutableLiveData<List<Recipe>>()

    private val _recetasFiltradas = MutableLiveData<List<Recipe>>()
    val recetasFiltradas: LiveData<List<Recipe>> = _recetasFiltradas

    private val _categoriaSeleccionada = MutableLiveData("todas")
    val categoriaSeleccionada: LiveData<String> = _categoriaSeleccionada

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        // TODO: reemplazar con Firebase cuando esté lista la conexión
        val recetas = listOf(
            Recipe(1, "Omelette Francés", 7,  listOf("rapidas")),
            Recipe(2, "Sandwiches",       15, listOf("rapidas")),
            Recipe(3, "Tacos con carne",  15, listOf("todas")),
            Recipe(4, "Tiramisú",         30, listOf("postres")),
            Recipe(5, "Ensalada Verde",   10, listOf("vegano")),
            Recipe(6, "Hamburguesa",      20, listOf("todas"))
        )
        _todasLasRecetas.value = recetas
        _recetasFiltradas.value = recetas
    }

    fun filtrar(categoria: String) {
        _categoriaSeleccionada.value = categoria
        _recetasFiltradas.value = if (categoria == "todas") {
            _todasLasRecetas.value
        } else {
            _todasLasRecetas.value?.filter { it.categorias.contains(categoria) }
        }
    }

    fun agregarReceta(recipe: Recipe) {
        val listaActual = _todasLasRecetas.value?.toMutableList() ?: mutableListOf()
        listaActual.add(recipe)
        _todasLasRecetas.value = listaActual
        filtrar(_categoriaSeleccionada.value ?: "todas")
    }
}