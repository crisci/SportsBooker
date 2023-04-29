package com.example.lab2.database.reservation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.lab2.database.court.Court
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "reservations", foreignKeys = [ForeignKey(entity = Court::class, parentColumns = ["courtId"], childColumns = ["courtId"])])
data class Reservation (
    @PrimaryKey(autoGenerate = true)
    val reservationId: Int = 0,
    @ColumnInfo(name = "courtId", index = true)
    val courtId: Int,
    val numOfPlayers: Int,
    val price: Double,
    val date: LocalDate,
    var time: LocalTime,
)

fun Reservation.formatPrice(): String {
    return String.format("%.02f", price)
}