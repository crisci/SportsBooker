package com.example.lab2.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.Court
import com.example.lab2.database.player.PlayerRepository
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NewMatchesVM @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val calendarVM: CalendarVM
): ViewModel() {

    private var sportFilter = MutableLiveData<String?>(null)
    fun getSportFilter(): LiveData<String?> {
        return sportFilter
    }
    fun setSportFilter(value: String?) {
        sportFilter.value = value
    }

    private var timeSlotFilter = MutableLiveData<LocalDate?>(null)
    fun getTimeslotFilter(): LocalDate? {
        return timeSlotFilter.value
    }

    private var mapNewMatches = MutableLiveData<Map<Court,List<Reservation>>>(emptyMap())
    fun getMapNewMatches() : LiveData<Map<Court,List<Reservation>>> {
        return mapNewMatches
    }

    fun refreshNewMatches(date: LocalDate, time: LocalTime) {
        var tmpList: List<ReservationWithCourt>
        viewModelScope.launch {
            if (sportFilter.value != null) {
                Log.d("calendarDate",calendarVM.getSelectedDate().value!!.toString())
                tmpList = reservationRepository.getAvailableReservationsByDateAndSport(
                    date,
                    time,
                    sportFilter.value!!,
                    1
                )
            }
            else {
                tmpList = reservationRepository.getAvailableReservationsByDate(
                    date,
                    time,
                    1
                )
            }
            mapNewMatches.value = tmpList
                .groupBy({ it.court }, { it.reservation })
                .mapValues { it.value.toList() }
        }
    }
}