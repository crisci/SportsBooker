package com.example.lab2.database.player

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lab2.database.reservation.ReservationWithCourt
import com.google.android.material.circularreveal.CircularRevealHelper.Strategy

@Dao
interface PlayerDAO {

    @Query("SELECT * FROM players WHERE playerId = (:playerId)")
    fun loadPlayerById(playerId: Int) : LiveData<Player>

    @Query("SELECT r.*, c.* FROM reservations r" +
            " JOIN courts c" +
            " ON c.courtId = r.courtId" +
            " JOIN players_reservations pr" +
            " ON pr.reservationId = r.reservationId" +
            " WHERE pr.playerId = :playerId"
    )
    fun loadReservationsByPlayerId(playerId: Int) : List<ReservationWithCourt>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayer(player: Player)

    @Query("DELETE FROM players")
    fun deletePlayers()

}