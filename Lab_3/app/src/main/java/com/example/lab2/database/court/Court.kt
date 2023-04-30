package com.example.lab2.database.court

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courts")
data class Court (
    @PrimaryKey(autoGenerate = true)
    val courtId: Int = 0,
    val name: String,
    val sport: String,
    val maxNumOfPlayers: Int
)