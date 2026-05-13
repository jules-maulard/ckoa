package com.example.ckoa.core.country.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class CountryAssetReader(
    private val context: Context
) {
    suspend fun readCountries(): List<CountryEntity> = withContext(Dispatchers.IO) {
        val jsonArray = readCountriesFromAssets()
        val countries = mutableListOf<CountryEntity>()

        for (i in 0 until jsonArray.length()) {
            val countryObject = jsonArray.getJSONObject(i)

            val isoCode = countryObject.optString("cca3")
            if (isoCode.isEmpty()) continue

            val name = extractFrenchName(countryObject)
            val capital = extractFirstCapital(countryObject)
            val currency = extractFirstCurrencyName(countryObject)
            val languages = extractLanguagesAsJsonString(countryObject)

            countries.add(
                CountryEntity(
                    isoCode = isoCode,
                    name = name,
                    capital = capital,
                    currency = currency,
                    languages = languages,
                )
            )
        }

        return@withContext countries
    }

    private fun readCountriesFromAssets(): JSONArray {
        val inputStream = context.assets.open("countries.json")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.use { it.readText() }
        return JSONArray(jsonString)
    }

    private fun extractFrenchName(countryObject: JSONObject): String {
        val nameObject = countryObject.optJSONObject("name") ?: return ""
        val translationsObject = nameObject.optJSONObject("translations")
        val frenchTranslation = translationsObject?.optJSONObject("fra")
        return frenchTranslation?.optString("common") ?: nameObject.optString("common", "")
    }

    private fun extractFirstCapital(countryObject: JSONObject): String {
        val capitalArray = countryObject.optJSONArray("capital")
        return if (capitalArray != null && capitalArray.length() > 0) {
            capitalArray.getString(0)
        } else {
            ""
        }
    }

    private fun extractFirstCurrencyName(countryObject: JSONObject): String {
        val currenciesObject = countryObject.optJSONObject("currencies")
        if (currenciesObject != null && currenciesObject.keys().hasNext()) {
            val firstKey = currenciesObject.keys().next()
            val currencyElement = currenciesObject.optJSONObject(firstKey)
            return currencyElement?.optString("name", "") ?: ""
        }
        return ""
    }

    private fun extractLanguagesAsJsonString(countryObject: JSONObject): String {
        val languagesObject = countryObject.optJSONObject("languages")
        val languagesArray = JSONArray()
        if (languagesObject != null) {
            val keys = languagesObject.keys()
            while (keys.hasNext()) {
                languagesArray.put(languagesObject.getString(keys.next()))
            }
        }
        return languagesArray.toString()
    }
}