package com.example.ckoa.core.country.data

import com.example.ckoa.core.country.domain.Country
import com.example.ckoa.core.country.domain.CountryRepository
import org.json.JSONArray

class CountryRepositoryImpl (
    private val countryDao: CountryDao,
    private val countryAssetReader: CountryAssetReader
) : CountryRepository {
    override suspend fun getCountryByIsoCode(isoCode: String): Country? {
        val entity = countryDao.getCountryByIsoCode(isoCode) ?: return null
        return mapEntityToDomain(entity)
    }

    override suspend fun getAllCountries(): List<Country> {
        val entities = countryDao.getAllCountries()
        return entities.map { mapEntityToDomain(it) }
    }

    override suspend fun populateDatabaseFromAssets() {
        val countriesToInsert = countryAssetReader.readCountries()
        countryDao.insertCountries(countriesToInsert)
    }

    private fun mapEntityToDomain(entity: CountryEntity): Country {
        val languagesList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(entity.languages)
            for (i in 0 until jsonArray.length()) {
                languagesList.add(jsonArray.getString(i))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return Country(
            isoCode = entity.isoCode,
            name = entity.name,
            capital = entity.capital,
            currency = entity.currency,
            languages = languagesList,
            geoJsonCoordinates = entity.geoJsonCoordinates,
            flagDrawableName = entity.flagDrawableName
        )
    }
}