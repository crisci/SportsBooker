package com.example.lab2.entities.database

data class ReservationWithCourt(
    val reservation: Reservation,
    val court: Court,
)