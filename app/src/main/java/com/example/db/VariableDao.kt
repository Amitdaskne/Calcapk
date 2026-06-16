package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {
    @Query("SELECT * FROM calc_variables")
    fun getAllVariables(): Flow<List<VariableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVariable(variable: VariableEntity)

    @Query("DELETE FROM calc_variables")
    suspend fun clearVariables()
}
