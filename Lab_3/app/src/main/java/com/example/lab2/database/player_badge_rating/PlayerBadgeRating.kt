package com.example.lab2.database.player_badge_rating

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.lab2.database.player.Player

@Entity(
    tableName = "player_badge_ratings",
    primaryKeys = ["playerId","badge","sport"],
    foreignKeys = [ForeignKey(entity = Player::class, parentColumns = ["playerId"], childColumns = ["playerId"])]
    )
data class PlayerBadgeRating (
    val playerId: Int,
    val badge: String,
    val rating: Int,
    val sport: String,
)