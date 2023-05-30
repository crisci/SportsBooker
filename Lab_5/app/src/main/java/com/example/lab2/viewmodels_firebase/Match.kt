package com.example.lab2.viewmodels_firebase

import com.example.lab2.entities.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate
import java.time.LocalTime

data class Match(
    val matchId: String,
    val numOfPlayers: Long,
    val date: LocalDate,
    val time: LocalTime

)


fun firebaseToMatch(d: DocumentSnapshot): Match {
     return  Match(
         d.id,
         d.getLong("numOfPlayers")!!,
         TimestampUtil.timestampToLocalDate(d.getTimestamp("timestamp")!!),
         TimestampUtil.timestampToLocalTime(d.getTimestamp("timestamp")!!)
     )
}