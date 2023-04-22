package com.example.lab2.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@ActivityRetainedScoped
class CalendarViewModel @Inject constructor() {
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    fun selectedDate(): LiveData<LocalDate> {
        return _selectedDate
    }
    fun setSelectedDate(value: LocalDate) {
        _selectedDate.value = value
    }
}
