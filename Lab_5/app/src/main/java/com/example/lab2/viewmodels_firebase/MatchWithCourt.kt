package com.example.lab2.viewmodels_firebase

import com.example.lab2.entities.Equipment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@kotlinx.serialization.Serializable
class MatchWithCourt (
    var match : Match,
    val court : Court,
)

