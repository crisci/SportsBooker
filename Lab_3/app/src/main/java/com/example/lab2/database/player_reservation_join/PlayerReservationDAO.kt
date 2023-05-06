package com.example.lab2.database.player_reservation_join

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.entities.Equipment
import java.time.LocalDate

@Dao
interface PlayerReservationDAO {

    @Query("INSERT INTO players_reservations VALUES (:playerId, :reservationId, :listEquipments, :finalPrice)")
    fun confirmReservation(playerId: Int, reservationId: Int, listEquipments: List<Equipment>, finalPrice: Double)

    @Query("DELETE FROM players_reservations WHERE reservationId=:reservationId")
    fun deletePlayerReservationByReservationId(reservationId: Int)

    @Query("DELETE FROM players_reservations")
    fun deletePlayerReservation()

    @Query("UPDATE players_reservations SET reservationId = :newReservationId, equipments = :newEquipments, finalPrice = :newFinalPrice WHERE playerId = :playerId AND reservationId = :reservationId")
    suspend fun updateReservation(playerId: Int, reservationId: Int, newReservationId: Int, newEquipments: List<Equipment>, newFinalPrice: Double)

    @Query("SELECT c.*, r.* FROM courts c JOIN reservations r" +
            " ON c.courtId = r.courtId AND r.date = :date AND r.numOfPlayers < c.maxNumOfPlayers" +
            " AND r.reservationId NOT IN" +
            " (SELECT pr.reservationId FROM players_reservations pr " +
            " WHERE playerId = :playerId)" +
            " GROUP BY c.courtId")
    fun getPlayerAvailableReservationsByDate(date: LocalDate, playerId: Int): List<CourtWithReservations>
}