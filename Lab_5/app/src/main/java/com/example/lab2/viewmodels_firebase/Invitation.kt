package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.player.Player
import com.example.lab2.entities.User
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime


sealed class Notification(
    open val id: String?,
    open val timestamp: Timestamp
)

data class MatchToReview(
    val match: Match,
    val court: Court,
    override val id: String?,
    override val timestamp: Timestamp
) : Notification(id, timestamp)

data class Invitation(
    val sender: User,
    val match: Match,
    val court: Court,
    override val id: String?,
    override val timestamp: Timestamp
) : Notification(id, timestamp)