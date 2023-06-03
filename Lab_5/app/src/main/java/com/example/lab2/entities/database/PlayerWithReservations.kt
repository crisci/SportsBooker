package com.example.lab2.entities.database

data class ReservationWithPlayers(
    val reservation: Reservation,
    val player: List<Player>,
)