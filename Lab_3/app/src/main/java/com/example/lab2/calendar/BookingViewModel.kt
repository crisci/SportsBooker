package com.example.lab2.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.ReservationWithCourt
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import javax.inject.Inject

@ActivityRetainedScoped
class BookingViewModel @Inject constructor(): ViewModel() {
    var personalPrice = MutableLiveData<Double>()
    fun setPersonalPrice(value: Double) {
        personalPrice.value = value
    }

    companion object {
        private val MY_STATIC_LIST = listOf("item1", "item2", "item3")
    }

    // ... other ViewModel code ...

    fun getStaticList(): List<String> {
        return MY_STATIC_LIST
    }
}
