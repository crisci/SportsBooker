package com.example.lab2.database.player_badge_rating

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayerBadgeRatingDAO { 

    @Query("SELECT * FROM player_badge_ratings WHERE playerId = (:playerId) AND sport = (:sport)")
    fun loadPlayerBadgesById(playerId: Int, sport: String) : LiveData<PlayerBadgeRating>
}