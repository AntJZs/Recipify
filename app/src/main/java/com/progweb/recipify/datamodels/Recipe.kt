package com.progweb.recipify.datamodels

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

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

    @get:PropertyName("ingredients") @set:PropertyName("ingredients")
    var ingredients: List<String> = emptyList(),

    @get:PropertyName("area") @set:PropertyName("area")
    var area: String = "",

    var id: String = ""
) : Serializable
