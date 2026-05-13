package com.example.ckoa.core.country.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_table")
data class CountryEntity(
    @PrimaryKey val isoCode: String,
    val name: String,
    val capital: String,
    val currency: String,
    val languages: String,
)
