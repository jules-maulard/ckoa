package com.example.ckoa.core.country.domain

interface CountryRepository {
    suspend fun getCountryByIsoCode(isoCode: String): Country?
    suspend fun getAllCountries(): List<Country>
    suspend fun populateDatabaseFromAssets()
}