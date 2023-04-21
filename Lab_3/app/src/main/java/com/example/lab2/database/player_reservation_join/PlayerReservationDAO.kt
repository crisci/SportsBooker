package com.example.lab2.database.player_reservation_join

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayerReservationDAO {

    @Query("SELECT * FROM players_reservations WHERE playerId = (:playerId)")
    fun loadPlayerReservationById(playerId: Int) : LiveData<PlayerReservation>
}