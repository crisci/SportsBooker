package com.example.lab2.database.player_reservation_join

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.lab2.database.player.Player
import com.example.lab2.database.reservation.Reservation

@Entity(
    tableName = "players_reservations",
    primaryKeys = ["playerId", "reservationId"],
    foreignKeys = [
            ForeignKey(Player::class, parentColumns = ["playerId"], childColumns = ["playerId"]),
            ForeignKey(Reservation::class, parentColumns = ["reservationId"], childColumns = ["reservationId"])
        ]
    )
data class PlayerReservation (
    val playerId: Int,
    val reservationId : Int
)