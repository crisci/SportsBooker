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
    var personalPrice = MutableLiveData<Double>()
    fun setPersonalPrice(value: Double) {
        personalPrice.value = value
    }

    companion object {
        private val SPORT_EQUIPMENT_MAP = mapOf(
            "Tennis" to listOf(
                Equipment("Racket", 2.0),
                Equipment("Tennis balls", 1.5)
            ),
            "Soccer" to listOf(
                Equipment("Soccer ball", 5.0),
                Equipment("Shin guards", 3.5),
                Equipment("Cleats", 2.5)
            ),
            "Golf" to listOf(
                Equipment("Golf clubs", 5.5),
                Equipment("Golf balls", 1.5),
                Equipment("Golf cart", 30.0)
            )
        )
    }

    fun getListEquipments(sport: String): List<Equipment> {
        return SPORT_EQUIPMENT_MAP[sport]!!
    }
}
