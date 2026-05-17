package com.example.ckoa.feature.daily_shape_guess.domain

import com.example.ckoa.core.country.domain.CountryRepository

class SubmitShapeGuessUseCase (
    private val countryRepository: CountryRepository,
    private val calculateDistanceUseCase: CalculateDistanceUseCase
) {
    suspend operator fun invoke(guessedIsoCode: String, targetIsoCode: String): ShapeGuessResult {
        val guessedCountry = countryRepository.getCountryByIsoCode(guessedIsoCode)
            ?: throw IllegalArgumentException("Guessed country not found for ISO: $guessedIsoCode")

        val targetCountry = countryRepository.getCountryByIsoCode(targetIsoCode)
            ?: throw IllegalArgumentException("Target country not found for ISO: $targetIsoCode")

        val isCorrect = guessedIsoCode.equals(targetIsoCode, ignoreCase = true)

        val geographicHint = calculateDistanceUseCase(
            lat1 = guessedCountry.latitude,
            lon1 = guessedCountry.longitude,
            lat2 = targetCountry.latitude,
            lon2 = targetCountry.longitude
        )

        return ShapeGuessResult(
            guessedCountryName = guessedCountry.name,
            guessedIsoCode = guessedCountry.isoCode,
            distanceInKm = geographicHint.distanceInKm,
            bearingDegrees = geographicHint.bearingDegrees,
            isCorrect = isCorrect
        )
    }
}