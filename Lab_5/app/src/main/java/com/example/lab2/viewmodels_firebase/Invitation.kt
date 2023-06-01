package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.player.Player
import com.example.lab2.entities.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime

data class Invitation(
    val id: String? = null,
    val sender: User,
    val match: Match,
    val court: Court,
    val timestamp: Timestamp
)

fun invitationToFirebase(match: Match, sentBy: String, sentTo: User) : Map<String, Any>{

    val db = FirebaseFirestore.getInstance()

    val map : MutableMap<String, Any> = mutableMapOf()

    map["match"] = db.document("matches/${match.matchId}")
    map["seen"] = false
    map["sentBy"] = db.document("players/$sentBy")
    map["sentTo"] = db.document("players/${sentTo.userId}")
    map["timestamp"] = Timestamp.now()


    return map

}