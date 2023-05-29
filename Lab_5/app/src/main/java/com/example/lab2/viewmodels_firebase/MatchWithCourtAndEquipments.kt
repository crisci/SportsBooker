package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.reservation.Reservation
import com.example.lab2.entities.Equipment

data class MatchWithCourtAndEquipments (
    val match : Match,
    val court : Court,
    val equipments: List<Equipment>,
    val finalPrice: Double
    )

    fun MatchWithCourtAndEquipments.formatPrice(): String {
        return String.format("%.02f", finalPrice)
    }
