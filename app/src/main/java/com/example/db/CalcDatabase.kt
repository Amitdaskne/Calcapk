package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryItem::class, VariableEntity::class], version = 1, exportSchema = false)
abstract class CalcDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun variableDao(): VariableDao

    companion object {
        @Volatile
        private var INSTANCE: CalcDatabase? = null

        fun getDatabase(context: Context): CalcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalcDatabase::class.java,
                    "scientific_calculator_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
