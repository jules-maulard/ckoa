package com.example.ckoa.core.progress.domain

data class DailyGameState(
    val date: String,
    val currentStep: GameStep,
    val flagGameStatus: GameStatus,
    val flagGuesses: List<String>,
    val shapeGameStatus: GameStatus,
    val shapeGuesses: List<String>
)
