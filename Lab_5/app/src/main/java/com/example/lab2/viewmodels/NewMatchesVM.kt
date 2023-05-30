package com.example.lab2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.entities.Sport
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.firebaseToCourt
import com.example.lab2.viewmodels_firebase.firebaseToMatch
import com.example.lab2.viewmodels_firebase.toTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NewMatchesVM @Inject constructor(): ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private var sportFilter = MutableLiveData<String?>(null)

    private var _newMatches = MutableLiveData<Map<Court,List<Match>>>(emptyMap())
    val newMatchesVM: LiveData<Map<Court, List<Match>>> = _newMatches
    fun getNewMatches(): LiveData<Map<Court, List<Match>>> {
        return newMatchesVM
    }

    private var matchesListener: ListenerRegistration? = null

    fun loadNewMatches(playerId: String = "mbvhLWL5YbPoYIqRskD1XkVVILv1", date: LocalDate, time: LocalTime, interests: List<Sport>) {

        matchesListener?.remove()

        val formattedInterests = formatInterests(interests)

        val mapCourtMatches = mutableMapOf<Court, MutableList<Match>>()
        val participatingMatchIds = mutableListOf<String>()
        val startOfTimestamp = date.atStartOfDay().toTimestamp()
        val endOfTimestamp = date.atTime(LocalTime.MAX).toTimestamp()

        viewModelScope.launch(Dispatchers.IO) {
            val reservationsSnapshot = db.collection("reservations")
                .whereEqualTo("player", db.document("players/$playerId"))
                .get().await()

            for (reservationDoc in reservationsSnapshot.documents) {
                val matchRef = reservationDoc.getDocumentReference("match")
                if (matchRef != null) {
                    participatingMatchIds.add(matchRef.id)
                }
            }

            val matchesSnapshot = db.collection("matches")
                .whereGreaterThanOrEqualTo("timestamp", startOfTimestamp)
                .whereLessThanOrEqualTo("timestamp", endOfTimestamp)
                .addSnapshotListener { snapshot, _ ->
                    viewModelScope.launch(Dispatchers.IO) {
                        snapshot?.documents?.forEach { matchDocument ->
                            val courtRef =
                                matchDocument.getDocumentReference("court")?.get()?.await()
                            val court = firebaseToCourt(courtRef!!)
                            if(!participatingMatchIds.contains(matchDocument.id)) {
                                val match = firebaseToMatch(matchDocument)
                                if (mapCourtMatches.containsKey(court)) {
                                    mapCourtMatches[court]?.add(match)
                                } else {
                                    mapCourtMatches[court] = mutableListOf(match)
                                }
                            }
                        }
                        _newMatches.postValue(filterNewMatches(mapCourtMatches, time, formattedInterests))
                    }
                }
            matchesListener = matchesSnapshot
        }
    }

    private fun formatInterests(interests: List<Sport>): List<String> {
        return interests.map { i -> i.toString().lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    private fun filterNewMatches(mapCourtMatches: Map<Court, List<Match>>, time: LocalTime, interests: List<String>): Map<Court, List<Match>> {
        val sportFilter = getSportFilter().value
        if (sportFilter.isNullOrEmpty())
            return mapCourtMatches
        val filteredMap = mutableMapOf<Court, List<Match>>()
        for ((court, matches) in mapCourtMatches) {
            if (interests.contains(court.sport) && sportFilter == court.sport) {
                val filteredMatches = matches.filter { match ->
                    match.time.isAfter(time)
                }
                if (filteredMatches.isNotEmpty()) {
                    filteredMap[court] = filteredMatches
                }
            }
        }
        return filteredMap
    }

    override fun onCleared() {
        super.onCleared()
        matchesListener?.remove()
    }


    fun getSportFilter(): LiveData<String?> {
        return sportFilter
    }
    fun setSportFilter(value: String?) {
        sportFilter.value = value
    }
    fun getMapNewMatches() : LiveData<Map<Court,List<Match>>> {
        return _newMatches
    }
}