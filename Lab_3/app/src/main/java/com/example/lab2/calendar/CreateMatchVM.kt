package com.example.lab2.calendar

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.motion.utils.ViewState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.CourtRepository
import com.example.lab2.database.player_reservation_join.PlayerReservationRepository
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CreateMatchVM @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val playerReservationRepository: PlayerReservationRepository,
    private val courtRepository: CourtRepository
): ViewModel() {

    private var listTimeslots : MutableLiveData<List<String>> = MutableLiveData(
        listOf("08:30","10:00", "11:30", "13:00", "14:30", "16:00", "17:30",
            "19:00", "20:30", "22:00")
    )

    fun getListTimeslots() : LiveData<List<String>> {
        return listTimeslots
    }

    private var exceptionMessage = MutableLiveData<String>()
    fun getExceptionMessage() : LiveData<String> {
        return exceptionMessage
    }


    fun createMatch(
        date: LocalDate,
        time: LocalTime,
        sport: String
    ) {

        viewModelScope.launch() {
                var newMatchId: Long
                var availableCourtId: Int

                availableCourtId = courtRepository.getFirstAvailableCourtForSportDateTime(
                    sport,
                    date,
                    time
                )

                if(availableCourtId == 0) {
                    exceptionMessage.value = "No available courts at this time"
                    return@launch
                }

                var match = Reservation(
                    date = date,
                    time = time,
                    courtId = availableCourtId,
                    numOfPlayers = 1,
                    price = 7.0
                )

                try {
                    newMatchId = reservationRepository.saveReservation(match)
                } catch (e: SQLiteConstraintException) {
                    exceptionMessage.value = "There is already a match at this time"
                    return@launch
                }


                try {
                    playerReservationRepository.confirmReservation(
                        1,
                        newMatchId.toInt(),
                        emptyList(),
                        7.0
                    )
                } catch (e: SQLiteConstraintException) {
                    exceptionMessage.value = "You already have a reservation at this time"
                    return@launch
                }

                exceptionMessage.value = "Match created successfully"
            }
        }
}