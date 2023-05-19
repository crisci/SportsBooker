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
import com.example.lab2.entities.User
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
    private val playerRepository: PlayerRepository,
    private val reservationRepository: ReservationRepository
    ): ViewModel() {

    var user = MutableLiveData<User>(User())
    var listBookedReservations = MutableLiveData<MutableSet<Int>>(mutableSetOf())

    fun setUser(value: User) {
        user.value = value
    }

    fun getUser(): User {
        return user.value!!
    }

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

    private val allMyReservations = MutableLiveData<List<ReservationWithCourt>>()
    fun getAllMyReservations() : LiveData<List<ReservationWithCourt>> {
        return allMyReservations
    }
    private val myFilteredReservations = MutableLiveData<List<ReservationWithCourt>>()
    fun getMyFilteredReservations() : LiveData<List<ReservationWithCourt>> {
        return myFilteredReservations
    }
    var mapNewMatches = MutableLiveData<Map<Court,List<Reservation>>>(emptyMap())
    fun getMapNewMatches() : LiveData<Map<Court,List<Reservation>>> {
        return mapNewMatches
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

    fun refreshNewMatches() {
        var tmpList: List<ReservationWithCourt>
        viewModelScope.launch {
            if (sportFilter.value != null) {
                tmpList = reservationRepository.getAvailableReservationsByDateAndSport(
                    selectedDate.value!!,
                    selectedTime.value!!,
                    sportFilter.value!!
                )
            }
            else {
                tmpList = reservationRepository.getAvailableReservationsByDate(
                    selectedDate.value!!,
                    selectedTime.value!!
                )
            }
            tmpList = filterUnbookedReservations(tmpList)
            tmpList = filterBySportAndTimeslot(tmpList)
            mapNewMatches.value = tmpList
                .groupBy({ it.court }, { it.reservation })
                .mapValues { it.value.toList() }
        }
    }

    private fun filterBySportAndTimeslot(allMyReservations: List<ReservationWithCourt>) : List<ReservationWithCourt> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) {
            return allMyReservations.filter { it.reservation.time == selectedTime.value || it.reservation.time.isAfter(selectedTime.value) }
        }
        return allMyReservations.filter { it.court.sport == sportFilter && it.reservation.time.isAfter(selectedTime.value) }
    }
    
    private fun filterUnbookedReservations(tmpList: List<ReservationWithCourt>): List<ReservationWithCourt> {
        return tmpList.filter { !listBookedReservations.value!!.contains(it.reservation.reservationId) }
    }

}
