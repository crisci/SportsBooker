package com.example.lab2.database.court_review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.lab2.database.court.Court
import com.example.lab2.database.player.Player
import com.example.lab2.database.reservation.Reservation
import com.google.gson.Gson

data class CourtReview(
    val courtId: String,
    val review: String,
    val ratingParameters: Map<String, Float>
)