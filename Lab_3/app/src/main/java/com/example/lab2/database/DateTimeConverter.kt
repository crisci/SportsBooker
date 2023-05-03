package com.example.lab2.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class DateTimeConverter {

    private val formatterDate: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    private val formatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    @TypeConverter
    fun fromDate(localDate: LocalDate?): String? {
        return localDate?.format(formatterDate)
    }

    @TypeConverter
    fun toLocalDate(sqlDate: String?): LocalDate? {
        return sqlDate?.let { LocalDate.parse(it,formatterDate)}
    }

    @TypeConverter
    fun fromTime(localTime: LocalTime?): String? {
        return localTime?.format(formatterTime)
    }

    @TypeConverter
    fun toLocalTime(sqlTime: String?): LocalTime? {
        return sqlTime?.let { LocalTime.parse(it,formatterTime)}
    }
}