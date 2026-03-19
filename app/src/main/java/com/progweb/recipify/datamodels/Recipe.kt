package com.progweb.recipify.datamodels

data class Recipe(
    val id: Int,
    val nombre: String,
    val tiempo: Int,
    val categorias: List<String>,
    val imagenRes: Int? = null
)