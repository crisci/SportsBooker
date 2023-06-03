package com.example.lab2.entities.database

import java.time.LocalTime

data class ReservationTimeslot(
    val reservationId: Int,
    val time: LocalTime
)