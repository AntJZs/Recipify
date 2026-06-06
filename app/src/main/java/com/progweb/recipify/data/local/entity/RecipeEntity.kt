package com.progweb.recipify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalTimeMinutes: Long,
    val categoryJson: String,
    val description: String,
    val imageURL: String,
    val ingredientsJson: String,
    val area: String,
    val body: String,
    val userId: String,
    val source: String
)
