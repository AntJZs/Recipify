package com.progweb.recipify.datamodels

import com.google.firebase.firestore.PropertyName

data class Recipe(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("totalTimeMinutes") @set:PropertyName("totalTimeMinutes")
    var totalTimeMinutes: Long = 0,

    @get:PropertyName("category") @set:PropertyName("category")
    var category: List<String> = emptyList(),

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("imageURL") @set:PropertyName("imageURL")
    var imageURL: String = "",

    var id: String = "" // Firestore document ID
)