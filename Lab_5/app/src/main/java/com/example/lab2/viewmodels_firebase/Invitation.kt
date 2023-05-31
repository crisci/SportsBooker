package com.example.lab2.viewmodels_firebase

import com.example.lab2.database.player.Player
import com.example.lab2.entities.User
import java.time.LocalDate
import java.time.LocalTime

data class Invitation(
    val id: String? = null,
    val sender: User,
    val match: Match,
    val court: Court,
    val date: LocalDate,
    val time: LocalTime
)