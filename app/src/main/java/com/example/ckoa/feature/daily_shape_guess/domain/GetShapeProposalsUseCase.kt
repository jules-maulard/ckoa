package com.example.ckoa.feature.daily_shape_guess.domain

import com.example.ckoa.core.country.domain.Country
import com.example.ckoa.core.country.domain.CountryRepository

class GetShapeProposalsUseCase (
    private val countryRepository: CountryRepository
) {
    suspend operator fun invoke(searchQuery: String): List<Country> {
        val allCountries = countryRepository.getAllCountries()

        if (searchQuery.isBlank()) {
            return allCountries.sortedBy { it.name }
        }

        val formattedQuery = searchQuery.trim().lowercase()

        return allCountries
            .filter { country ->
                country.name.lowercase().contains(formattedQuery)
            }
            .sortedBy { it.name }
    }
}