package com.example.lab2.firebase_models

import com.google.firebase.firestore.DocumentSnapshot

@kotlinx.serialization.Serializable
class MatchWithCourt(
    var match: Match,
    val court: Court,
)

fun firebaseToMatchWithCourt(m: DocumentSnapshot, c: DocumentSnapshot): MatchWithCourt {
    return MatchWithCourt(
        firebaseToMatch(m),
        firebaseToCourt(c)
    )
}
