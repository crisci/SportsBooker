package com.example.lab2.entities.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lab2.utils.DateConverter
import java.time.LocalDate


data class Player(
    val playerId: Int = 0,
    val fullName: String,
    val location: String,
    @TypeConverters(DateConverter::class)
    val dateOfBirth: LocalDate,
    val email: String,
    val profileImage: String,
)