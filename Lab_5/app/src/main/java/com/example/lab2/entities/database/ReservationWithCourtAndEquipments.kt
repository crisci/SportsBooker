package com.example.lab2.entities.database

import com.example.lab2.entities.Equipment

data class ReservationWithCourtAndEquipments(
    val reservation: Reservation,
    val court: Court,
    val equipments: List<Equipment>,
    val finalPrice: Double
)

fun ReservationWithCourtAndEquipments.formatPrice(): String {
    return String.format("%.02f", finalPrice)
}