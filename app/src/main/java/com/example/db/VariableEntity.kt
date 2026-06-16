package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calc_variables")
data class VariableEntity(
    @PrimaryKey val name: String,
    val realValue: Double,
    val imagValue: Double = 0.0
)
