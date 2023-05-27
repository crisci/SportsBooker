package com.example.lab2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarVM @Inject constructor() : ViewModel() {

    private var selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    fun getSelectedDate() : LiveData<LocalDate> {
        return selectedDate
    }
    fun setSelectedDate(value: LocalDate) {
        selectedDate.value = value
    }

    var selectedTime = MutableLiveData<LocalTime>(LocalTime.of(10,0))
    fun getSelectedTime() : LiveData<LocalTime> {
        return selectedTime
    }

    private val showTutorial = MutableLiveData<Boolean>(true)
    fun getShowTutorial() : LiveData<Boolean> {
        return showTutorial
    }
    fun setShowTutorial(value: Boolean) {
        showTutorial.value = value
    }

}