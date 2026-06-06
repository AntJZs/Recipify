package com.progweb.recipify.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.progweb.recipify.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE source = 'firestore'")
    fun getFirestoreRecipes(): Flow<List<RecipeEntity>>

    @Upsert
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)
}
