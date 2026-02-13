package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behealthy.app.core.database.entity.PoemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {
    @Query("SELECT * FROM poems")
    fun getAllPoems(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE category = :category")
    fun getPoemsByCategory(category: String): Flow<List<PoemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(poems: List<PoemEntity>)
    
    @Query("SELECT COUNT(*) FROM poems")
    suspend fun getCount(): Int
}
