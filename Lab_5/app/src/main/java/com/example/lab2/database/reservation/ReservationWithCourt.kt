package com.example.lab2.database.reservation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.lab2.database.court.Court
import com.example.lab2.entities.Equipment

data class ReservationWithCourt (
    @Embedded val reservation : Reservation,

    @Relation(
        parentColumn = "courtId",
        entityColumn = "courtId"
    )
    val court : Court,
)