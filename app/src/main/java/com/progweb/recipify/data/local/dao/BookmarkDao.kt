package com.progweb.recipify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.progweb.recipify.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT recipeId FROM bookmarks")
    fun getAll(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(bookmarks: List<BookmarkEntity>) {
        deleteAll()
        insertAll(bookmarks)
    }
}
