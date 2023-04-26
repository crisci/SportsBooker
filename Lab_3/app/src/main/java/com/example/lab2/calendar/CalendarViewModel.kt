package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import javax.inject.Inject

@ActivityRetainedScoped
class CalendarViewModel @Inject constructor(): ViewModel() {
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    fun setSelectedDate(value: LocalDate) {
        selectedDate.value = value
    }
}
