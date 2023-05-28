package com.example.lab2.viewmodels_firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class MatchFirebase(
    val Court: DocumentReference,
    val numOfPlayers: Long?,
    val timestamp: Timestamp?,
    val listOfPlayers: List<DocumentReference>?
)

