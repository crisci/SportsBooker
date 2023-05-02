package com.example.lab2.database.reservation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.player_reservation_join.ReservationWithPlayers
import java.time.LocalDate

@Dao
interface ReservationDAO {


    // TODO FIX
    @Query("SELECT r.*, c.name, c.sport FROM reservations r LEFT OUTER JOIN courts c ON c.courtId = r.courtId")
    fun loadAllReservations(): List<ReservationWithCourt>

    //Not necessary to define update because of REPLACE strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReservation(reservation: Reservation)

    @Query("DELETE FROM reservations WHERE reservationId = (:reservationId)")
    fun cancelReservationById(reservationId: Int)

    @Query("DELETE FROM reservations")
    fun deleteAllReservations()

    @Query(
        "SELECT r.*, pr.* " +
            "FROM reservations r" +
            " JOIN players_reservations pr ON pr.reservationId = r.reservationId" +
            " WHERE r.reservationId = (:reservationId)"
    )
    fun loadPlayersByReservationId(reservationId: Int) : ReservationWithPlayers

    @Query("UPDATE reservations " +
            "SET numOfPlayers = numOfPlayers + (:num)" +
            "WHERE reservationId = :reservationId")
    fun updateNumOfPlayers(reservationId: Int, num: Int)




}