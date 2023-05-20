package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
class MyReservationsVM @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val vm: CalendarVM
    ): ViewModel() {

    var user = MutableLiveData<User>(User())

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


    private val myReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    fun getMyReservations() : LiveData<List<ReservationWithCourtAndEquipments>> {
        return myReservations
    }

    fun refreshMyReservations() {
        viewModelScope.launch {
            myReservations.value =
                playerRepository.loadReservationsByPlayerId(
                    1,
                    vm.getSelectedDate().value!!
                )
            myReservations.value = filterMyReservationsBySportAndTimeslot(myReservations.value!!)
        }
    }

    private fun filterMyReservationsBySportAndTimeslot(allMyReservations: List<ReservationWithCourtAndEquipments>) : List<ReservationWithCourtAndEquipments> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) {
            return allMyReservations.filter { it.reservation.time == vm.getSelectedTime().value || it.reservation.time.isAfter(vm.getSelectedTime().value) }
        }
        return allMyReservations.filter { it.court.sport == sportFilter && (it.reservation.time == vm.getSelectedTime().value || it.reservation.time.isAfter(vm.getSelectedTime().value)) }
    }
}
