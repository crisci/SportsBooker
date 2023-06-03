package com.example.lab2.database.reservation

import java.time.LocalTime

data class ReservationTimeslot(
    val reservationId: Int,
    val time: LocalTime
)