package com.example.ckoa.core

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DatabaseConverters {

    private val gson = Gson()

    @TypeConverter
    fun stringListToJson(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun jsonToStringList(json: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, listType) ?: emptyList()
    }
}