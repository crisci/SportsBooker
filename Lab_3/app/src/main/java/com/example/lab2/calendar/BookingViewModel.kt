package com.example.lab2.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.entities.Equipment
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import javax.inject.Inject

@ActivityRetainedScoped
class BookingViewModel @Inject constructor(): ViewModel() {

}
