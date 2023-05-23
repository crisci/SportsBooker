package com.example.lab2.database.reservation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.lab2.database.court.Court
import com.example.lab2.entities.Equipment

data class ReservationWithCourtAndEquipments (
    @Embedded val reservation : Reservation,

    @Relation(
        parentColumn = "courtId",
        entityColumn = "courtId"
    )
    val court : Court,
    val equipments: List<Equipment>,
    val finalPrice: Double
)

fun ReservationWithCourtAndEquipments.formatPrice(): String {
    return String.format("%.02f", finalPrice)
}