package com.example.lab2.database.court

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate

@Dao
interface CourtDAO {

    @Query("SELECT * FROM courts WHERE courtId = :courtId")
    fun loadCourtById(courtId: Int) : LiveData<Court>

    @Query("SELECT * FROM courts")
    fun loadAllCourts(): List<Court>
    @Insert
    fun save(court: Court)

    @Query("SELECT c.*, r.* FROM courts c JOIN reservations r" +
            " ON c.courtId = r.courtId AND r.date = :date AND r.numOfPlayers < c.maxNumOfPlayers")
    fun getAvailableReservationsByDate(date: LocalDate): List<CourtWithReservations>

    @Query("SELECT c.*, r.* FROM courts c JOIN reservations r" +
            " ON c.courtId = r.courtId AND r.date = :date AND r.numOfPlayers < c.maxNumOfPlayers" +
            " WHERE r.reservationId NOT IN" +
            " (SELECT pr.reservationId FROM players_reservations pr WHERE pr.playerId = :playerId)")
    fun getReservationsByDate(date: LocalDate, playerId: Int): List<CourtWithReservations>
}