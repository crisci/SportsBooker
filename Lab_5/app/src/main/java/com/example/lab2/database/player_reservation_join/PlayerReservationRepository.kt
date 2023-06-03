package com.example.lab2.database.player_reservation_join

import com.example.lab2.entities.Equipment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class PlayerReservationRepository @Inject constructor(private val playerReservationDAO: PlayerReservationDAO) {

    suspend fun confirmReservation(
        playerId: Int,
        reservationId: Int,
        listEquipments: List<Equipment>,
        finalPrice: Double
    ) = withContext(Dispatchers.IO) {
        playerReservationDAO.confirmReservation(
            playerId,
            reservationId,
            listEquipments,
            finalPrice
        )
    }

    suspend fun deletePlayerReservationByReservationId(reservationId: Int) =
        withContext(Dispatchers.IO) {
            playerReservationDAO.deletePlayerReservationByReservationId(reservationId)
        }

    suspend fun deletePlayerReservation() =
        withContext(Dispatchers.IO) { playerReservationDAO.deletePlayerReservation() }

    suspend fun updateReservation(
        playerId: Int,
        reservationId: Int,
        newReservationId: Int,
        newEquipments: List<Equipment>,
        newFinalPrice: Double
    ) = withContext(Dispatchers.IO) {
        playerReservationDAO.updateReservation(
            playerId,
            reservationId,
            newReservationId,
            newEquipments,
            newFinalPrice
        )
    }

    suspend fun getPlayerAvailableReservationsByDate(
        date: LocalDate,
        playerId: Int,
        sport: String
    ) =
        withContext(Dispatchers.IO) {
            playerReservationDAO.getPlayerAvailableReservationsByDate(
                date,
                playerId,
                sport
            )
        }
}