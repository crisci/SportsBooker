package com.example.lab2.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court_review.CourtReviewRepository
import com.example.lab2.database.player.PlayerRepository
import com.example.lab2.database.reservation.ReservationRepository
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Equipment
import com.example.lab2.entities.Sport
import com.example.lab2.entities.Statistic
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.TimestampUtil
import com.example.lab2.viewmodels_firebase.firebaseToCourt
import com.example.lab2.viewmodels_firebase.firebaseToMatch
import com.example.lab2.viewmodels_firebase.firebaseToMatchWithCourtAndEquipments
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MyReservationsVM @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val reservationRepository: ReservationRepository,
    private val courtReviewRepository: CourtReviewRepository,
    ): ViewModel() {

    private val db = FirebaseFirestore.getInstance()

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


    //private val myReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    private val myStatistics = MutableLiveData<List<Statistic>>()


    private var _myReservations = MutableLiveData<List<MatchWithCourtAndEquipments>>()
    val res: LiveData<List<MatchWithCourtAndEquipments>> = _myReservations

    private var _listener: ListenerRegistration? = null;

    fun getMyReservations() : LiveData<List<MatchWithCourtAndEquipments>> {
        return res
    }

    fun getMyStatistics() : LiveData<List<Statistic>> {
        return myStatistics
    }

    fun refreshMyStatistics(playerId: Int) {
        viewModelScope.launch {
            myStatistics.value =
                playerRepository.loadAllReservationsByPlayerId(
                    1,
                ).groupBy { it.court.sport }.map {
                    when(it.key.lowercase()){
                        "baseball" -> Statistic(sport = Sport.BASEBALL, gamesPlayed = it.value.size)
                        "basketball" -> Statistic(sport = Sport.BASKETBALL, gamesPlayed = it.value.size)
                        "golf" -> Statistic(sport = Sport.GOLF, gamesPlayed = it.value.size)
                        "padel" -> Statistic(sport = Sport.PADEL, gamesPlayed = it.value.size)
                        "soccer" -> Statistic(sport = Sport.SOCCER, gamesPlayed = it.value.size)
                        "tennis" -> Statistic(sport = Sport.TENNIS, gamesPlayed = it.value.size)
                        else -> throw java.lang.RuntimeException("Wrong sport in database entry!")
                    }
                }
        }
    }

    fun refreshMyReservations(date: LocalDate, time: LocalTime, interests: List<Sport>) {
        _listener?.remove()

        val formattedInterests = formatInterests(interests)

        _listener = FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("player", db.document("players/mbvhLWL5YbPoYIqRskD1XkVVILv1"))
            .addSnapshotListener { documents, error ->
                CoroutineScope(Dispatchers.IO).launch {
                    val list = processDocuments(documents)
                    Log.i("Reservations", list.toString())
                    Log.e("sport", formattedInterests.toString())
                    _myReservations.postValue(filterList(list, date, time, formattedInterests))
                }
            }
    }

    private fun formatInterests(interests: List<Sport>): List<String> {
        return interests.map { i -> i.toString().lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    private suspend fun processDocuments(documents: QuerySnapshot?): List<MatchWithCourtAndEquipments> {
        return documents?.documents?.mapNotNull { reservation ->
            val matchRef = reservation.getDocumentReference("match")?.get()?.await()
            val courtRef = matchRef?.getDocumentReference("court")?.get()?.await()
            if (matchRef != null && courtRef != null) {
                firebaseToMatchWithCourtAndEquipments(matchRef, courtRef, reservation)
            } else null
        } ?: emptyList()
    }

    private fun filterList(list: List<MatchWithCourtAndEquipments>, date: LocalDate, time: LocalTime, interests: List<String>): List<MatchWithCourtAndEquipments> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) return list
        return list.filter {
            it.match.date == date &&
                    it.match.time >= time &&
                    interests.contains(it.court.sport)
                    && sportFilter == it.court.sport
        }
    }

    private fun filterMyReservationsBySportAndTimeslot(allMyReservations: List<ReservationWithCourtAndEquipments>, time: LocalTime, interests: List<Sport>) : List<ReservationWithCourtAndEquipments> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty()) {
            return allMyReservations.filter { r -> ( r.reservation.time == time || r.reservation.time.isAfter(time) ) && interests.any { it.toString().lowercase() == r.court.sport.lowercase() } }.filter { it.reservation.time >= LocalTime.now() || it.reservation.date > LocalDate.now() }
        }
        return allMyReservations.filter { it.court.sport == sportFilter && (it.reservation.time == time || it.reservation.time.isAfter(time)) }.filter { it.reservation.time >= LocalTime.now() || it.reservation.date > LocalDate.now() }
    }

    suspend fun getReservationDetails(reservationId: Int): ReservationWithCourtAndEquipments {
        return reservationRepository.getReservationDetails(reservationId)
    }

    suspend fun getCourtAvgReviews(courtId: Int): Float {
        val review = courtReviewRepository.getAvgReviewByCourtId(courtId)
        return if(review != null) (review.cleanlinessRating + review.lightingRating + review.maintenanceRating)/3 else 0.toFloat()
    }


}
