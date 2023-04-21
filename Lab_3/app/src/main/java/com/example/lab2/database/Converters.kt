package com.example.lab2.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime


class Converters {
    @TypeConverter
    fun fromDate(localDate: LocalDate?): Long? {
        return localDate?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(sqlDate: Long?): LocalDate? {
        return sqlDate?.let { LocalDate.ofEpochDay(it)}
    }

    @TypeConverter
    fun fromTime(localTime: LocalTime?): Long? {
        return localTime?.toNanoOfDay()
    }

    @TypeConverter
    fun toLocalTime(sqlTime: Long?): LocalTime? {
        return sqlTime?.let { LocalTime.ofNanoOfDay(it)}
    }
}