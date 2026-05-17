package com.example.ckoa.feature.daily_shape_guess.domain

data class ShapeGuessResult(
    val guessedCountryName: String,
    val guessedIsoCode: String,
    val distanceInKm: Long,
    val bearingDegrees: Float,
    val isCorrect: Boolean
)
