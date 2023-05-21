package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.player_reservation_join.PlayerReservationRepository
import com.example.lab2.database.reservation.ReservationTimeslot
import com.example.lab2.entities.Sport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditReservationViewModel @Inject constructor(
   private val playerReservationRepository: PlayerReservationRepository
) : ViewModel() {

    private var listReservationTimeslot = MutableLiveData<List<ReservationTimeslot>>(
        mutableListOf()
    )

    fun getListReservationTimeslot() : LiveData<List<ReservationTimeslot>> {
        return listReservationTimeslot
    }

    fun getReservationTimeslot(date: LocalDate, playerId: Int, reservationId: Int, sport: String, existingTimeslot: LocalTime) {
        viewModelScope.launch {

           var list = playerReservationRepository.getPlayerAvailableReservationsByDate(
               date, playerId, sport)

           var firstTimeslot : MutableList<ReservationTimeslot> = mutableListOf(ReservationTimeslot(reservationId,existingTimeslot))
           listReservationTimeslot.value = listReservationTimeslot.value!!.plus(firstTimeslot as MutableList<ReservationTimeslot>).sortedBy { it.time } as MutableList<ReservationTimeslot>
        }
    }


    fun addList (list: List<ReservationTimeslot>) {
        listReservationTimeslot.value?.plus(list)
    }

}