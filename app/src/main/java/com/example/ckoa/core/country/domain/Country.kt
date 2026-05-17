package com.example.ckoa.core.country.domain

data class Country(
    val isoCode: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val capital: String,
    val currency: String,
    val languages: List<String>,
    val geoJsonCoordinates: String?,
    val flagDrawableName: String
)