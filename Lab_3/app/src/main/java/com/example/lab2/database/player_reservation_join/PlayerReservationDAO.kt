package com.example.lab2.database.player_reservation_join

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerReservationDAO {

    @Query("INSERT INTO players_reservations VALUES (:playerId, :reservationId)")
    fun confirmReservation(playerId: Int, reservationId: Int)

    @Query("DELETE FROM players_reservations")
    fun deletePlayerReservation()

}