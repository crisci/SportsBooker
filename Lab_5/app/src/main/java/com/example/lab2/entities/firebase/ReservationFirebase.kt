package com.example.lab2.entities.firebase

import com.google.firebase.firestore.DocumentReference

data class ReservationFirebase(
    val match: DocumentReference,
    val player: DocumentReference,
    val listOfEquipments: List<String>?,
    val finalPrice: Long?
)
