package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.player_reservation_join.PlayerReservationRepository
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.database.reservation.ReservationTimeslot
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Equipment
import com.example.lab2.entities.Sport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditReservationViewModel @Inject constructor(
   private val playerReservationRepository: PlayerReservationRepository,
   private val reservationRepository: ReservationRepository
) : ViewModel() {

    private var availableTimeslots = MutableLiveData<MutableList<ReservationTimeslot>>(
        mutableListOf()
    )
    fun getAvailableTimeslot() : LiveData<MutableList<ReservationTimeslot>> {
        return availableTimeslots
    }

    private var currentReservation = MutableLiveData<ReservationWithCourtAndEquipments>()
    fun getCurrentReservation() : LiveData<ReservationWithCourtAndEquipments> {
        return currentReservation
    }
    fun setCurrentReservation(reservation: ReservationWithCourtAndEquipments) {
        currentReservation.value = reservation
    }

    private var newReservation = MutableLiveData<ReservationTimeslot>()
    fun getSelectedReservation() : LiveData<ReservationTimeslot> {
        return newReservation
    }
    fun setNewReservation(reservation: ReservationTimeslot) {
        newReservation.value = reservation
    }

    fun fetchAvailableReservations(
        date: LocalDate,
        playerId: Int,
        reservationId: Int,
        sport: String,
        existingTimeslot: LocalTime
    ) {
        viewModelScope.launch {
            val otherReservations =
                playerReservationRepository.getPlayerAvailableReservationsByDate(date, playerId, sport)

            // Include the timeslot that is already reserved and add it as the first element of the list
            val existingReservationTime = ReservationTimeslot(reservationId, existingTimeslot)
            val availableReservations = mutableListOf(existingReservationTime)
            availableReservations.addAll(otherReservations)

            availableTimeslots.value = availableReservations.sortedBy { it.time }.toMutableList()
        }
    }

    fun updateReservation(playerId: Int, newReservationId: Int, newFinalPrice: Double, newEquipments: List<Equipment>) {
        val oldReservationId = currentReservation.value!!.reservation.reservationId
        viewModelScope.launch {
            playerReservationRepository
                .updateReservation(
                    playerId,
                    oldReservationId,
                    newReservationId,
                    newEquipments,
                    newFinalPrice
                )
            reservationRepository
                .updateNumOfPlayers(oldReservationId)
            reservationRepository
                .updateNumOfPlayers(newReservationId)
        }
    }
}