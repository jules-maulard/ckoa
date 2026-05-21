package com.example.ckoa.feature.daily_shape_guess.presentation

import com.example.ckoa.core.country.domain.Country
import com.example.ckoa.core.progress.domain.GameStatus
import com.example.ckoa.feature.daily_shape_guess.domain.ShapeGuessResult

data class ShapeGuessState(
    val isLoading: Boolean = true,
    val targetShapeGeoJson: String = "",
    val searchQuery: String = "",
    val proposals: List<Country> = emptyList(),
    val guesses: List<ShapeGuessResult> = emptyList(),
    val gameStatus: GameStatus = GameStatus.PENDING,
    val errorMessage: String? = null
)