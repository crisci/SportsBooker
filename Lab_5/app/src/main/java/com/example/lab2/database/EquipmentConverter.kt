package com.example.lab2.database

import androidx.room.TypeConverter
import com.example.lab2.entities.Equipment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class EquipmentConverter {

    @TypeConverter
    fun toString(value: List<Equipment>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun fromString(value: String): List<Equipment> {
        val listType = object : TypeToken<List<Equipment>>() {}.type
        return Gson().fromJson(value, listType)
    }
}