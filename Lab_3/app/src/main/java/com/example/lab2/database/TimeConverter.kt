package com.example.lab2.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class TimeConverter {

    private val formatterTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromTime(localTime: LocalTime?): String? {
        return localTime?.format(formatterTime)
    }

    @TypeConverter
    fun toLocalTime(sqlTime: String?): LocalTime? {
        return sqlTime.let { LocalTime.parse(it,formatterTime)}
    }
}