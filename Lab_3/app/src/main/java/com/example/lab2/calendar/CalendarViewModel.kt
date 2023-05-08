package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@ActivityRetainedScoped
class CalendarViewModel @Inject constructor(): ViewModel() {
    var selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    var list = MutableLiveData<List<ReservationWithCourtAndEquipments>>(emptyList())
    var mapCourtReservations = MutableLiveData<Map<Court,List<Reservation>>>(emptyMap())
    fun setSelectedDate(value: LocalDate) {
        selectedDate.value = value
    }
}
