package com.progweb.recipify.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.progweb.recipify.data.local.AppDatabase
import com.progweb.recipify.data.local.entity.BookmarkEntity
import com.progweb.recipify.data.local.entity.RecipeEntity
import com.progweb.recipify.datamodels.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeRepository private constructor(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private var syncStarted = false

    val firestoreRecipes: Flow<List<Recipe>> =
        db.recipeDao().getFirestoreRecipes().map { list -> list.map { it.toRecipe() } }

    val bookmarkedIds: Flow<Set<String>> =
        db.bookmarkDao().getAll().map { it.toSet() }

    fun startFirestoreSync() {
        if (syncStarted) return
        syncStarted = true
        syncRecipes()
        syncBookmarks()
    }

    private fun syncRecipes() {
        firestore.collection("recipe")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                    return@addSnapshotListener
                }
                val entities = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.also { it.id = doc.id }
                }.map { it.toEntity("firestore") }
                scope.launch { db.recipeDao().upsertAll(entities) }
            }
    }

    private fun syncBookmarks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("bookmarks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                    return@addSnapshotListener
                }
                val bookmarks = snapshot.documents.map { BookmarkEntity(it.id) }
                scope.launch { db.bookmarkDao().replaceAll(bookmarks) }
            }
    }

    fun refreshBookmarkSync() {
        syncBookmarks()
    }

    suspend fun deleteRecipe(recipeId: String) {
        db.recipeDao().deleteById(recipeId)
        firestore.collection("recipe").document(recipeId).delete().await()
    }

    suspend fun saveRecipe(recipe: Recipe) {
        db.recipeDao().upsert(recipe.toEntity("local"))
        firestore.collection("recipe").add(recipe.toFirestoreMap())
    }

    private fun Recipe.toEntity(source: String) = RecipeEntity(
        id = id,
        name = name,
        totalTimeMinutes = totalTimeMinutes,
        categoryJson = gson.toJson(category),
        description = description,
        imageURL = imageURL,
        ingredientsJson = gson.toJson(getFormattedIngredients()),
        area = area,
        body = body,
        userId = userId,
        source = source
    )

    private fun RecipeEntity.toRecipe(): Recipe {
        val catType = object : TypeToken<List<String>>() {}.type
        val ingType = object : TypeToken<List<String>>() {}.type
        return Recipe(
            name = name,
            totalTimeMinutes = totalTimeMinutes,
            category = gson.fromJson(categoryJson, catType),
            description = description,
            imageURL = imageURL,
            ingredients = gson.fromJson(ingredientsJson, ingType),
            area = area,
            body = body,
            userId = userId,
            id = id
        )
    }

    private fun Recipe.toFirestoreMap() = mapOf(
        "name" to name,
        "totalTimeMinutes" to totalTimeMinutes,
        "category" to category,
        "description" to description,
        "imageURL" to imageURL,
        "ingredients" to ingredients,
        "area" to area,
        "body" to body,
        "userId" to userId
    )

    companion object {
        @Volatile private var INSTANCE: RecipeRepository? = null

        fun getInstance(context: Context): RecipeRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecipeRepository(
                    AppDatabase.getInstance(context),
                    FirebaseFirestore.getInstance()
                ).also { INSTANCE = it }
            }
    }
}
