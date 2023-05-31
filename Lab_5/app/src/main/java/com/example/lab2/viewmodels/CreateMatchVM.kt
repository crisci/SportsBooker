package com.example.lab2.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.CourtRepository
import com.example.lab2.database.player_reservation_join.PlayerReservationRepository
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.viewmodels_firebase.MatchFirebase
import com.example.lab2.viewmodels_firebase.ReservationFirebase
import com.example.lab2.viewmodels_firebase.toTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateMatchVM @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val playerReservationRepository: PlayerReservationRepository,
    private val courtRepository: CourtRepository
): ViewModel() {

    val db = FirebaseFirestore.getInstance()

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
        viewModelScope.launch {
            val startTimestamp = LocalDateTime.of(date, time).toTimestamp()
            val endTimestamp = LocalDateTime.of(date, time).plusNanos(999_999_999).toTimestamp()

            val listOfBookedCourts = mutableListOf<String>()
            //Populate the list of already booked courts for the given timeslot
            db.collection("matches")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThan("timestamp", endTimestamp)
                .get().addOnSuccessListener { matches ->
                    CoroutineScope(Dispatchers.IO).launch {
                        for (match in matches) {
                            val q = match.getDocumentReference("court")?.get()?.await()
                            listOfBookedCourts.add(q!!.id)
                        }
                        if (listOfBookedCourts.isEmpty()) listOfBookedCourts.add((-1).toString())
                        //Extract the first available court for the given timeslot and sport
                        db.collection("courts")
                            .whereNotIn(FieldPath.documentId(), listOfBookedCourts)
                            .whereEqualTo("sport", sport).limit(1).get().addOnSuccessListener {court ->
                                if(court.isEmpty) {
                                    exceptionMessage.postValue("No available courts at this time")
                                } else {
                                    val match = db.collection("matches").add(
                                        MatchFirebase(
                                            court.first().reference,
                                            1,
                                            LocalDateTime.of(date, time).toTimestamp(),
                                            // TODO: Replace with user ID
                                            listOf(db.collection("players").document("HhkmyV1SqjVEsVt83Ld65I0cF9x2"))
                                        )
                                    ).addOnSuccessListener { matchAdded ->
                                        db.collection("reservations").add(
                                            ReservationFirebase(
                                                matchAdded,
                                                // TODO: Replace with user ID
                                                db.collection("players").document("HhkmyV1SqjVEsVt83Ld65I0cF9x2"),
                                                emptyList(),
                                                court.first().getLong("basePrice")!!
                                            )
                                        )
                                    }
                                    exceptionMessage.postValue("Match created successfully")
                                }
                            }
                    }
                }
        }
    }




    fun createMatchOld(
        date: LocalDate,
        time: LocalTime,
        sport: String
    ) {

        viewModelScope.launch() {
            val newMatchId: Long
            val availableCourtId: Int

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

    fun filterTimeslots(date: LocalDate) {
        if(date > LocalDate.now()) {
            listTimeslots.value = listOf("08:30","10:00", "11:30", "13:00", "14:30", "16:00", "17:30",
                "19:00", "20:30", "22:00")
        }
        else {
            listTimeslots.value = listTimeslots.value!!.filter {
                val time = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                time.isAfter(LocalTime.now())
            }
        }
    }
}