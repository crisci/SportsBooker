package com.example.lab2.viewmodels_firebase

import android.util.Log
import com.example.lab2.entities.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.Contextual
import java.time.LocalDate
import java.time.LocalTime

@kotlinx.serialization.Serializable
data class Match(
    val matchId: String,
    val numOfPlayers: Long,
    @kotlinx.serialization.Serializable(with = DateTimeSerializers.LocalDateSerializer::class)
    val date: LocalDate,
    @kotlinx.serialization.Serializable(with = DateTimeSerializers.LocalTimeSerializer::class)
    var time: LocalTime,
    var listOfPlayers : MutableList<String>
)


fun firebaseToMatch(d: DocumentSnapshot): Match {

    val mappedPlayers = (d.get("listOfPlayers") as List<DocumentReference>).map {
        it.id
    }.toMutableList()

     return  Match(
         d.id,
         d.getLong("numOfPlayers")!!,
         TimestampUtil.timestampToLocalDate(d.getTimestamp("timestamp")!!),
         TimestampUtil.timestampToLocalTime(d.getTimestamp("timestamp")!!),
         mappedPlayers
     )
}