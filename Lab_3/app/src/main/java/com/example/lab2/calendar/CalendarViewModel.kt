package com.example.lab2.calendar

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.player.PlayerRepository
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
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

    private var selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    fun getSelectedDate() : LiveData<LocalDate> {
        return selectedDate
    }
    fun setSelectedDate(value: LocalDate) {
        selectedDate.value = value
    }

    var selectedTime = MutableLiveData<LocalTime>(LocalTime.of(LocalTime.now().hour,0))
    fun getSelectedTime() : String {
        return selectedTime.value!!.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private val allMyReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    fun getAllMyReservations() : LiveData<List<ReservationWithCourtAndEquipments>> {
        return allMyReservations
    }
    private val myFilteredReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    fun getMyFilteredReservations() : LiveData<List<ReservationWithCourtAndEquipments>> {
        return myFilteredReservations
    }

    fun refreshMyReservations() {
        viewModelScope.launch {
            Log.d("dateInsideRefresh",selectedDate.value.toString())
            allMyReservations.value =
                playerRepository.loadReservationsByPlayerId(
                    1,
                    selectedDate.value!!
                )
            myFilteredReservations.value = filterBySportAndTimeslot(allMyReservations.value!!)
        }
    }

    private fun filterBySportAndTimeslot(allMyReservations: List<ReservationWithCourtAndEquipments>) : List<ReservationWithCourtAndEquipments> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) {
            return allMyReservations.filter { it.reservation.time == selectedTime.value || it.reservation.time.isAfter(selectedTime.value) }
        }
        return allMyReservations.filter { it.court.sport == sportFilter && it.reservation.time.isAfter(selectedTime.value) }
    }

    var mapCourtReservations = MutableLiveData<Map<Court,List<Reservation>>>(emptyMap())
}
