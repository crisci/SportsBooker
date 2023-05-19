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
import java.time.LocalTime

@Dao
interface ReservationDAO {


    // TODO FIX
    @Query("SELECT r.*, c.name, c.sport FROM reservations r LEFT OUTER JOIN courts c ON c.courtId = r.courtId")
    fun getAllReservations(): LiveData<List<ReservationWithCourt>>

    @Query("SELECT * FROM reservations WHERE date = :date")
    fun getReservationsByDate(date: LocalDate): LiveData<List<Reservation>>

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
            "SET numOfPlayers = (SELECT COUNT(*) FROM players_reservations WHERE reservationId = :reservationId)" +
            "WHERE reservationId = :reservationId")
    fun updateNumOfPlayers(reservationId: Int)


    @Query("SELECT c.*, r.* FROM courts c JOIN reservations r" +
            " ON c.courtId = r.courtId AND r.date = :date AND time >= :time AND r.numOfPlayers < c.maxNumOfPlayers" +
            " GROUP BY c.courtId, r.reservationId")
    fun getAvailableReservationsByDate(date: LocalDate, time: LocalTime): List<ReservationWithCourt>

    @Query("SELECT c.*, r.* FROM courts c JOIN reservations r" +
            " ON c.courtId = r.courtId AND r.date = :date AND time >= :time AND r.numOfPlayers < c.maxNumOfPlayers AND c.sport = :sport" +
            " GROUP BY c.courtId, r.reservationId")
    fun getAvailableReservationsByDateAndSport(date: LocalDate, time: LocalTime, sport: String): List<ReservationWithCourt>


}