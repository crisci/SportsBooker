package com.example.lab2.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore


abstract class Notification(
    open val id: String? = null,
    open val timestamp: Timestamp = Timestamp.now()
)

data class MatchToReview(
    val match: Match,
    val court: Court,
    override val id: String?,
    override val timestamp: Timestamp
) : Notification()

data class Invitation(
    val sender: User,
    val match: Match,
    val court: Court,
    override val id: String?,
    override val timestamp: Timestamp
) : Notification()

fun invitationToFirebase(match: Match, sentBy: String, sentTo: User): Map<String, Any> {

    val db = FirebaseFirestore.getInstance()

    val map: MutableMap<String, Any> = mutableMapOf()

    map["match"] = db.document("matches/${match.matchId}")
    map["seen"] = false
    map["sentBy"] = db.document("players/$sentBy")
    map["sentTo"] = db.document("players/${sentTo.userId}")
    map["timestamp"] = Timestamp.now()


    return map

}