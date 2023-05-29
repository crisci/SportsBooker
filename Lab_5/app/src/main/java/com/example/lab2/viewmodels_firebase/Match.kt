package com.example.lab2.viewmodels_firebase

import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate
import java.time.LocalTime

data class Match(
    val matchId: String,
    val numOfPlayers: Long,
    val date: LocalDate,
    var time: LocalTime,
)


fun firebaseToMatch(d: DocumentSnapshot): Match {
     return  Match(
         d.id,
         d.getLong("numOfPlayers")!!,
         TimestampUtil.timestampToLocalDate(d.getTimestamp("timestamp")!!),
         TimestampUtil.timestampToLocalTime(d.getTimestamp("timestamp")!!)
     )
}