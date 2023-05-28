package com.example.lab2.viewmodels_firebase

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimestampUtil {
    companion object {
        fun timestampToLocalDate(timestamp: com.google.firebase.Timestamp): LocalDate {
            val instant = timestamp?.toDate()?.toInstant()
            val zoneId = ZoneId.systemDefault() // Or choose a specific ZoneId
            val zonedDateTime = instant?.atZone(zoneId)
            val localDate = zonedDateTime?.toLocalDate()
            return localDate!!
        }

        fun timestampToLocalTime(timestamp: com.google.firebase.Timestamp): LocalTime {
            val instant = timestamp?.toDate()?.toInstant()
            val zoneId = ZoneId.systemDefault() // Or choose a specific ZoneId
            val zonedDateTime = instant?.atZone(zoneId)
            val localTime = zonedDateTime?.toLocalTime()
            return localTime!!
        }
    }
}