package com.example.lab2.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.Equipment
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.scopes.ViewScoped
import java.time.LocalDate
import javax.inject.Inject


class FilterViewModel @Inject constructor(): ViewModel() {

    var sportFilter = MutableLiveData<String?>(null)
    var timeSlotFilter = MutableLiveData<LocalDate?>(null)

    fun setSportFilter(value: String?) {
        sportFilter.value = value
    }

    fun getSportFilter(): String? {
        return sportFilter.value
    }

    fun setTimeslotFilter(value: String?) {
        timeSlotFilter.value = if (value != null) LocalDate.parse(value) else null
    }

    fun getTimeslotFilter(): LocalDate? {
        return timeSlotFilter.value
    }

}