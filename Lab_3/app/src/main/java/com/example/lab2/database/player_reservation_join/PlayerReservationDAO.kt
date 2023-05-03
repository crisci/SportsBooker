package com.example.lab2.database.player_reservation_join

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lab2.entities.Equipment

@Dao
interface PlayerReservationDAO {

    @Query("INSERT INTO players_reservations VALUES (:playerId, :reservationId, :listEquipments)")
    fun confirmReservation(playerId: Int, reservationId: Int, listEquipments: List<Equipment>)

    @Query("DELETE FROM players_reservations WHERE reservationId=:reservationId")
    fun deletePlayerReservationByReservationId(reservationId: Int)

    @Query("DELETE FROM players_reservations")
    fun deletePlayerReservation()

}