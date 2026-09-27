package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spokenInput: String,
    val parsedExpression: String,
    val resultText: String,
    val isSuccess: Boolean,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
