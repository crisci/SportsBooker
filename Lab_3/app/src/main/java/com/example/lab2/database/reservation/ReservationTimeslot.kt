package com.example.lab2.database.reservation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.lab2.database.court.Court
import com.example.lab2.entities.Equipment
import java.time.LocalTime

data class ReservationTimeslot (
    val reservationId: Int,
    val time: LocalTime
)