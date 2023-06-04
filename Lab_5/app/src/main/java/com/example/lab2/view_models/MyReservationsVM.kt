package com.example.lab2.view_models


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.Sport
import com.example.lab2.entities.Statistic
import com.example.lab2.entities.User
import com.example.lab2.entities.MatchWithCourtAndEquipments
import com.example.lab2.entities.firebaseToMatchWithCourtAndEquipments
import com.google.firebase.auth.FirebaseAuth
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
class MyReservationsVM @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var sportFilter = MutableLiveData<String?>(null)
    fun getSportFilter(): LiveData<String?> {
        return sportFilter
    }

    fun setSportFilter(value: String?) {
        sportFilter.value = value
    }

    private var _timeSlotFilter = MutableLiveData<LocalDate?>(null)
    val timeslotFilter: LiveData<LocalDate?> = _timeSlotFilter

    private val myStatistics =
        MutableLiveData<
                Pair<List<Statistic>, MutableMap<String, Long>?>
                >()


    private var _myReservations = MutableLiveData<List<MatchWithCourtAndEquipments>>()
    val res: LiveData<List<MatchWithCourtAndEquipments>> = _myReservations

    private lateinit var _listener: ListenerRegistration

    init {
        startListener()
    }

    private fun startListener() {
        _listener = FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("player", db.document("players/${auth.currentUser!!.uid}"))
            .addSnapshotListener { documents, error ->
                CoroutineScope(Dispatchers.IO).launch {
                    val list = processDocuments(documents)
                    _myReservations.postValue(list)
                }
            }
    }

    fun stopListener() {
        _listener.remove()
    }

    fun getMyReservations(): LiveData<List<MatchWithCourtAndEquipments>> {
        return res
    }

    fun getMyStatistics(): LiveData<Pair<List<Statistic>, MutableMap<String, Long>?>> {
        return myStatistics
    }

    fun refreshMyStatistics(playerId: String) {

        FirebaseFirestore.getInstance().collection("reservations")
            .whereEqualTo("player", db.document("players/$playerId"))
            .get().addOnSuccessListener { documents ->
                CoroutineScope(Dispatchers.IO).launch {
                    val list = processStatistics(documents, playerId)

                    val playerRef = db.document("players/$playerId")
                        .get().await()
                    val player = User.fromFirebase(playerRef)

                    myStatistics.postValue(Pair(list, player.score))
                }
            }


    }

    private suspend fun processStatistics(
        documents: QuerySnapshot?,
        playerId: String
    ): List<Statistic> {

        val statistics = documents?.documents?.mapNotNull { reservation ->
            val matchRef = reservation.getDocumentReference("match")?.get()?.await()
            val courtRef = matchRef?.getDocumentReference("court")?.get()?.await()

            if (matchRef != null && courtRef != null) {
                val m = firebaseToMatchWithCourtAndEquipments(matchRef, courtRef, reservation)
                if (m.match.date.isBefore(LocalDate.now())) {
                    m.court.sport!! to 1
                } else null

            } else null
        }

        val sumBySport =
            statistics?.groupBy { it.first }?.mapValues { entry -> entry.value.sumOf { it.second } }
                ?.toList()

        return sumBySport?.map {
            when (it.first.lowercase()) {
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



    private fun formatInterests(interests: List<Sport>): List<String> {
        return interests.map { i ->
            i.toString().lowercase().replaceFirstChar { c -> c.uppercase() }
        }
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

    val _playerToShow: MutableLiveData<User> = MutableLiveData()
    val playerToShow: LiveData<User> get() = _playerToShow

    fun setPlayerToShow(u: User) {
        _playerToShow.value = u
    }

    fun filterList(sport: String?, date: LocalDate, time: LocalTime?): List<MatchWithCourtAndEquipments> {
        return when(_myReservations.value) {
            null -> listOf()
            else -> if (sport != null && time != null && date != null) {
                _myReservations.value!!.filter { it.court.sport == sport && it.match.time >= time && it.match.date == date }
            }
            else if (sport != null && time == null && date != null) {
                _myReservations.value!!.filter { it.court.sport == sport && it.match.date == date }
            }
            else if (sport != null && time != null && date == null) {
                _myReservations.value!!.filter { it.court.sport == sport && it.match.time >= time }

            }
            else if (sport != null && time == null && date == null) {
                _myReservations.value!!.filter { it.court.sport == sport }
            }
            else if (sport == null && time != null && date != null) {
                _myReservations.value!!.filter { it.match.time >= time && it.match.date == date }
            }
            else if (sport == null && time != null && date == null) {
                _myReservations.value!!.filter { it.match.time >= time }
            }
            else if (sport == null && time == null && date != null) {
                _myReservations.value!!.filter { it.match.date == date }
            }
            else _myReservations.value!!
        }
    }

}
