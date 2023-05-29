package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.reservation.Reservation
import com.example.lab2.entities.Equipment
import com.google.firebase.firestore.DocumentSnapshot

data class MatchWithCourtAndEquipments (
    val reservationId: String,
    val match : Match,
    val court : Court,
    val equipments: List<Equipment>,
    val finalPrice: Double
    )

fun firebaseToMatchWithCourtAndEquipments (m: DocumentSnapshot, c: DocumentSnapshot, r: DocumentSnapshot): MatchWithCourtAndEquipments {
    return MatchWithCourtAndEquipments(r.id, firebaseToMatch(m), firebaseToCourt(c), r.get("listOfEquipments") as List<Equipment>, r.getDouble("finalPrice")!!)
}

    fun MatchWithCourtAndEquipments.formatPrice(): String {
        return String.format("%.02f", finalPrice)
    }
