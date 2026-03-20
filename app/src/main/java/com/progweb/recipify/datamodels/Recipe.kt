package com.progweb.recipify.datamodels

data class Recipe(
    val id: String,
    val name: String,
    val totalTimeMinutes: Int,
    val category: List<String>,
    val imageURL: Int? = null
)