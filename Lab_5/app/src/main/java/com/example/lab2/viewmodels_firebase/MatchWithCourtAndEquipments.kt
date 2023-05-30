package com.example.lab2.viewmodels_firebase

import android.media.audiofx.DynamicsProcessing.Eq
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

    val mappedEquipments: List<Equipment> = (r.get("listOfEquipments") as List<String>).map {
        when(it.lowercase()) {
            "racket" -> Equipment("Racket", 2.0)
            "tennis balls" -> Equipment("Tennis balls", 1.5)
            "soccer ball" -> Equipment("Soccer ball", 5.0)
            "shin guards" -> Equipment("Shin guards", 3.5)
            "cleats" -> Equipment("Cleats", 2.5)
            "golf clubs" -> Equipment("Golf clubs", 5.5)
            "golf balls" -> Equipment("Golf balls", 1.5)
            "golf cart" -> Equipment("Golf cart", 30.0)
            else -> Equipment("Unknown", 0.0)
        }
    }

    return MatchWithCourtAndEquipments(r.id, firebaseToMatch(m), firebaseToCourt(c), mappedEquipments, r.getDouble("finalPrice")!!)
}

    fun MatchWithCourtAndEquipments.formatPrice(): String {
        return String.format("%.02f", finalPrice)
    }
