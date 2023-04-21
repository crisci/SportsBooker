package com.example.lab2.database.player

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayerDAO {

    @Query("SELECT * FROM players WHERE playerId = (:playerId)")
    fun loadPlayerById(playerId: Int) : LiveData<Player>
}