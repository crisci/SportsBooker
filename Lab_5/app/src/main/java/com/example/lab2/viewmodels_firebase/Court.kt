package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.player.Player
import com.example.lab2.entities.Sport
import com.google.firebase.firestore.DocumentSnapshot

data class Court(
    val courtId: String,
    val description: String?,
    val maxNumberOfPlayers: Long?,
    val name: String?,
    val sport: String?,
    val basePrice: Double?
)


fun firebaseToCourt(d: DocumentSnapshot): Court {
    return Court(
        d.id,
        d.getString("description"),
        d.getLong("maxNumOfPlayers"),
        d.getString("name"),
        d.getString("sport"),
        d.getDouble("basePrice")
    )
}