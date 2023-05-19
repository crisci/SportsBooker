package com.example.lab2.database.reservation

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.lab2.database.player_reservation_join.ReservationWithPlayers
import java.time.LocalDate
import java.time.LocalTime

class ReservationRepository(private val database: ReservationDAO) {

    suspend fun loadAllReservations() = database.getAllReservations()

    suspend fun getReservationsByDate(date: LocalDate) = database.getReservationsByDate(date)

    suspend fun saveReservation(reservation: Reservation) = database.saveReservation(reservation)

    suspend fun cancelReservationById(reservationId: Int) = database.cancelReservationById(reservationId)

    suspend fun deleteAllReservations() = database.deleteAllReservations()

    suspend fun loadPlayersByReservationId(reservationId: Int) = database.loadPlayersByReservationId(reservationId)

    suspend fun updateNumOfPlayers(reservationId: Int) = database.updateNumOfPlayers(reservationId)

    suspend fun getAvailableReservationsByDate(date: LocalDate, time: LocalTime) = database.getAvailableReservationsByDate(date, time)

    suspend fun getAvailableReservationsByDateAndSport(date: LocalDate, time: LocalTime, sport: String) = database.getAvailableReservationsByDateAndSport(date, time, sport)
}