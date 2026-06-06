package com.progweb.recipify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.progweb.recipify.data.local.dao.BookmarkDao
import com.progweb.recipify.data.local.dao.RecipeDao
import com.progweb.recipify.data.local.entity.BookmarkEntity
import com.progweb.recipify.data.local.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipify.db"
                ).build().also { INSTANCE = it }
            }
    }
}
