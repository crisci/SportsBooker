package com.example.lab2.database.player

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lab2.database.DateConverter
import java.time.LocalDate

//TODO: Player view model with all the information about the current player

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val playerId: Int = 0,
    val fullName: String,
    val location: String,
    @TypeConverters(DateConverter::class)
    val dateOfBirth: LocalDate,
    val email: String,
    val profileImage: String,
)