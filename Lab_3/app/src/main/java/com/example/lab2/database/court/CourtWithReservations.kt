package com.example.lab2.database.court

import androidx.room.Embedded
import androidx.room.Relation
import com.example.lab2.database.reservation.Reservation

data class CourtWithReservations(
    @Embedded val court: Court,
    @Relation(
        parentColumn = "courtId",
        entityColumn = "courtId"
    )
    val reservations: List<Reservation>
)