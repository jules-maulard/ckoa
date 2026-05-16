package com.example.ckoa.core.progress.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_game_state_table")
data class DailyGameStateEntity(
    @PrimaryKey val date: String,
    val currentStep: String,
    val flagGameStatus: String,
    val flagGuesses: List<String>,
    val shapeGameStatus: String,
    val shapeGuesses: List<String>
)
