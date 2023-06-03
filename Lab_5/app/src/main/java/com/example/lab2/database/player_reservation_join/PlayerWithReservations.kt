package com.example.lab2.database.player_reservation_join

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.lab2.database.player.Player
import com.example.lab2.database.reservation.Reservation

data class ReservationWithPlayers(
    @Embedded
    val reservation: Reservation,

    @Relation(
        parentColumn = "reservationId",
        entityColumn = "playerId",
        associateBy = Junction(PlayerReservation::class)
    )
    val player: List<Player>,
)