package com.example.lab2.entities.database

import android.graphics.Bitmap
import androidx.room.TypeConverters

import com.example.lab2.utils.BitmapConverter

data class Court(
    val courtId: Int = 0,
    val name: String,
    val sport: String,
    val maxNumOfPlayers: Int,
    val description: String? = null,
    @TypeConverters(BitmapConverter::class)
    val courtPhoto: Bitmap? = null
)

