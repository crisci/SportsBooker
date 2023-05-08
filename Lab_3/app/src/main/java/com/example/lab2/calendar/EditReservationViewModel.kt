package com.example.lab2.calendar

import androidx.lifecycle.MutableLiveData
import com.example.lab2.database.reservation.ReservationTimeslot
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class EditReservationViewModel @Inject constructor() {
    var listReservationTimeslot = MutableLiveData<MutableList<ReservationTimeslot>>(
        mutableListOf()
    )

    fun addList (list: List<ReservationTimeslot>) {
        listReservationTimeslot.value?.plus(list)
    }
}