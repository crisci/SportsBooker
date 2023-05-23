package com.example.lab2.database.reservation

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.player_reservation_join.ReservationWithPlayers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class ReservationRepository @Inject constructor(private val database: ReservationDAO) {

    suspend fun loadAllReservations() = database.getAllReservations()

    suspend fun getReservationsByDate(date: LocalDate) = database.getReservationsByDate(date)

    suspend fun saveReservation(reservation: Reservation) =
        withContext(Dispatchers.IO) {
            database.saveReservation(reservation)
        }

    suspend fun cancelReservationById(reservationId: Int) = database.cancelReservationById(reservationId)

    suspend fun deleteAllReservations() = database.deleteAllReservations()

    suspend fun loadPlayersByReservationId(reservationId: Int) = database.loadPlayersByReservationId(reservationId)

    suspend fun updateNumOfPlayers(reservationId: Int) =
        withContext(Dispatchers.IO) { database.updateNumOfPlayers(reservationId) }

    suspend fun getReservationDetails(reservationId: Int): ReservationWithCourtAndEquipments =
        withContext(Dispatchers.IO) { database.loadReservationsByReservationId(reservationId) }

    suspend fun getAvailableReservationsByDate(date: LocalDate, time: LocalTime, playerId: Int): List<ReservationWithCourt> =
        withContext(Dispatchers.IO){ database.getAvailableReservationsByDate(date, time, playerId) }

    suspend fun getAvailableReservationsByDateAndSport(date: LocalDate, time: LocalTime, sport: String, playerId: Int): List<ReservationWithCourt> {
        return withContext(Dispatchers.IO) {
            database.getAvailableReservationsByDateAndSport(
                date,
                time,
                sport,
                playerId
            )
        }
    }
}