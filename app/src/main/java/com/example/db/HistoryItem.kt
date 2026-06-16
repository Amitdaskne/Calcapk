package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calc_history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val mode: String,
    val timestamp: Long = System.currentTimeMillis()
)
