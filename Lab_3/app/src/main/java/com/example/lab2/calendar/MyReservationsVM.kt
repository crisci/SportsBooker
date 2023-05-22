package com.example.lab2.calendar


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.Court
import com.example.lab2.database.court_review.CourtReviewRepository
import com.example.lab2.database.player.PlayerRepository
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Sport
import com.example.lab2.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MyReservationsVM @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val reservationRepository: ReservationRepository,
    private val courtReviewRepository: CourtReviewRepository,
    ): ViewModel() {



    var user = MutableLiveData<User>(User())


    fun setUser(value: User) {
        user.value = value
    }

    fun getUser(): User {
        return user.value!!
    }

    private var sportFilter = MutableLiveData<String?>(null)
    fun getSportFilter(): LiveData<String?> {
        return sportFilter
    }
    fun setSportFilter(value: String?) {
        sportFilter.value = value
    }

    private var timeSlotFilter = MutableLiveData<LocalDate?>(null)
    fun getTimeslotFilter(): LocalDate? {
        return timeSlotFilter.value
    }


    private val myReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    fun getMyReservations() : LiveData<List<ReservationWithCourtAndEquipments>> {
        return myReservations
    }

    fun refreshMyReservations(date: LocalDate, time: LocalTime, interests: List<Sport>) {
        viewModelScope.launch {
            myReservations.value =
                playerRepository.loadReservationsByPlayerId(
                    1,
                    date
                )
            myReservations.value = filterMyReservationsBySportAndTimeslot(myReservations.value!!, time, interests)
        }
    }

    private fun filterMyReservationsBySportAndTimeslot(allMyReservations: List<ReservationWithCourtAndEquipments>, time: LocalTime, interests: List<Sport>) : List<ReservationWithCourtAndEquipments> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) {
            return allMyReservations.filter { r -> ( r.reservation.time == time || r.reservation.time.isAfter(time) ) && interests.any { it.toString().lowercase() == r.court.sport.lowercase() } }
        }
        return allMyReservations.filter { it.court.sport == sportFilter && (it.reservation.time == time || it.reservation.time.isAfter(time)) }
    }

    suspend fun getReservationDetails(reservationId: Int): ReservationWithCourtAndEquipments {
        return reservationRepository.getReservationDetails(reservationId)
    }

    suspend fun getCourtAvgReviews(courtId: Int): Float {
        val review = courtReviewRepository.getAvgReviewByCourtId(courtId)
        return if(review != null) (review.cleanlinessRating + review.lightingRating + review.maintenanceRating)/3 else 0.toFloat()
    }


}
