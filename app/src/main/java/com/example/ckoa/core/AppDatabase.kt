package com.example.ckoa.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ckoa.core.country.data.CountryDao
import com.example.ckoa.core.country.data.CountryEntity

@Database(
    entities = [
        CountryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao
}