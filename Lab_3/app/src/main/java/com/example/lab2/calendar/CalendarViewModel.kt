package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@ActivityRetainedScoped
class CalendarViewModel @Inject constructor(): ViewModel() {
    var selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    var list = MutableLiveData<List<Reservation>>(emptyList())
    var mapCourtsWithAvailableTimeslots = MutableLiveData<Map<Court,Set<LocalTime>>>(emptyMap())
    fun setSelectedDate(value: LocalDate) {
        selectedDate.value = value
    }
}
