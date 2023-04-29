package com.example.lab2.database.player

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.lab2.database.reservation.ReservationWithCourt

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

}