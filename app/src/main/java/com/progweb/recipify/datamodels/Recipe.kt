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
    var ingredients: List<Any> = emptyList(),

    @get:PropertyName("area") @set:PropertyName("area")
    var area: String = "",

    @get:PropertyName("body") @set:PropertyName("body")
    var body: String = "",

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    var isBookmarked: Boolean = false,
    var id: String = ""
) : Serializable {

    fun getFormattedIngredients(): List<String> {
        return ingredients.map { item ->
            when (item) {
                is String -> item
                is Map<*, *> -> {
                    val quantity = item["quantity"] as? String ?: ""
                    val ingredient = item["ingredient"] as? String ?: ""
                    if (quantity.isNotEmpty()) "$quantity $ingredient" else ingredient
                }
                else -> item.toString()
            }
        }
    }

    fun getDisplayDescription(): String {
        return if (body.isNotEmpty()) body else description
    }
}
