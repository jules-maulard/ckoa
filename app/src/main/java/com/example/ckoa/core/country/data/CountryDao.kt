package com.example.ckoa.core.country.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Query("SELECT * FROM country_table WHERE isoCode = :isoCode")
    suspend fun getCountryByIsoCode(isoCode: String): CountryEntity?

    @Query("SELECT * FROM country_table")
    suspend fun getAllCountries(): List<CountryEntity>
}