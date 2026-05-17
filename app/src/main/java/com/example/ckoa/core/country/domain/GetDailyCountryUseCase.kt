package com.example.ckoa.core.country.domain

import kotlin.math.abs

class GetDailyCountryUseCase (
    private val countryRepository: CountryRepository
) {
    suspend operator fun invoke(currentDateString: String): Country {
        val allAvailableCountries = countryRepository.getAllCountries().sortedBy { it.isoCode }

        require(allAvailableCountries.isNotEmpty()) {
            "The countries database must be populated before fetching a daily country."
        }

        val dateHashSeed = currentDateString.hashCode()
        val dailyDeterministicIndex = abs(dateHashSeed) % allAvailableCountries.size

        return allAvailableCountries[dailyDeterministicIndex]
    }
}