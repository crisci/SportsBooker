package com.example.lab2.entities.database

import androidx.room.TypeConverters
import com.example.lab2.utils.DateConverter
import com.example.lab2.utils.TimeConverter
import java.time.LocalDate
import java.time.LocalTime

data class Reservation(
    val reservationId: Int = 0,
    val courtId: Int,
    val numOfPlayers: Int,
    val price: Double,
    @TypeConverters(DateConverter::class)
    val date: LocalDate,
    @TypeConverters(TimeConverter::class)
    var time: LocalTime,
)

fun Reservation.formatPrice(): String {
    return String.format("%.02f", price)
}