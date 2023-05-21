package com.example.lab2.database.court

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lab2.BitmapConverter

@Entity(tableName = "courts")
data class Court (
    @PrimaryKey(autoGenerate = true)
    val courtId: Int = 0,
    val name: String,
    val sport: String,
    val maxNumOfPlayers: Int,
    val description: String? = null,
    @TypeConverters(BitmapConverter::class)
    val courtPhoto: Bitmap? = null
)