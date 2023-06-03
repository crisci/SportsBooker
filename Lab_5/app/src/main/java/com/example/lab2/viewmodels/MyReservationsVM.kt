package com.example.lab2.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.Sport
import com.example.lab2.entities.Statistic
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
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
class MyReservationsVM @Inject constructor(): ViewModel() {

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

    fun refreshMyStatistics(playerId: String) {

        FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("player", db.document("players/$playerId"))
            .get().addOnSuccessListener { documents ->
                CoroutineScope(Dispatchers.IO).launch {
                    val list = processStatistics(documents)
                    myStatistics.postValue(list)
                }
            }
    }

    private suspend fun processStatistics(documents: QuerySnapshot?) : List<Statistic> {

        val statistics = documents?.documents?.mapNotNull { reservation ->
            val matchRef = reservation.getDocumentReference("match")?.get()?.await()
            val courtRef = matchRef?.getDocumentReference("court")?.get()?.await()

            if(matchRef != null && courtRef != null){
                val m = firebaseToMatchWithCourtAndEquipments(matchRef, courtRef, reservation)
                if(m.match.date.isBefore(LocalDate.now())){
                    m.court.sport!! to 1
                }else null

            } else null
        }

        val sumBySport = statistics?.groupBy { it.first }?.mapValues { entry -> entry.value.sumOf { it.second } }?.toList()

        return sumBySport?.map {
            when(it.first.lowercase()){
                "baseball" -> Statistic(sport = Sport.BASEBALL, gamesPlayed = it.second)
                "basketball" -> Statistic(sport = Sport.BASKETBALL, gamesPlayed = it.second)
                "golf" -> Statistic(sport = Sport.GOLF, gamesPlayed = it.second)
                "padel" -> Statistic(sport = Sport.PADEL, gamesPlayed = it.second)
                "football" -> Statistic(sport = Sport.FOOTBALL, gamesPlayed = it.second)
                "tennis" -> Statistic(sport = Sport.TENNIS, gamesPlayed = it.second)
                else -> throw RuntimeException("Unknown sport name found while retrieving statistics.")
            }
        } ?: listOf()


    }

    fun refreshMyReservations(userID: String, date: LocalDate, time: LocalTime, interests: List<Sport>) {
        _listener?.remove()

        val formattedInterests = formatInterests(interests)

        _listener = FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("player", db.document("players/$userID"))
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
        if (sportFilter.isNullOrEmpty()) {
            return list.filter {
                it.match.date == date &&
                        it.match.time >= time
            }
        }
        return list.filter {
            it.match.date == date &&
                    it.match.time >= time &&
                    interests.contains(it.court.sport)
                    && sportFilter == it.court.sport
        }
    }

    val _playerToShow : MutableLiveData<User> = MutableLiveData()
    val playerToShow : LiveData<User> get() = _playerToShow

    fun setPlayerToShow(u: User){
        _playerToShow.value = u
    }

}
