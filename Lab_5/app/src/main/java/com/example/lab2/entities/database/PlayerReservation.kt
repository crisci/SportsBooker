package com.example.lab2.entities.database

import androidx.room.TypeConverters
import com.example.lab2.utils.EquipmentConverter
import com.example.lab2.entities.Equipment


data class PlayerReservation(
    val playerId: Int,
    val reservationId: Int,
    @TypeConverters(EquipmentConverter::class)
    val equipments: List<Equipment>,
    val finalPrice: Double
)