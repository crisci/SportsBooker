package com.example.lab2.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.Equipment
import com.example.lab2.entities.Match
import com.example.lab2.entities.MatchWithCourtAndEquipments
import com.example.lab2.entities.MatchWithCourtAndEquipmentsToFirebase
import com.example.lab2.entities.firebaseToMatch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class EditReservationViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var _listener: ListenerRegistration? = null

    private var _availableMatches = MutableLiveData<MutableList<Match>>(mutableListOf())
    private val availableMatches: LiveData<MutableList<Match>> = _availableMatches

    private val _editedReservation = MutableLiveData<MatchWithCourtAndEquipments>()
    private var editedReservation: LiveData<MatchWithCourtAndEquipments> = _editedReservation

    var error: MutableLiveData<String?> = MutableLiveData()


    // Get available matches for the same day & court
    fun getAvailableMatches(): LiveData<MutableList<Match>> {
        return availableMatches
    }

    // Current reservation: id, match, equipments, etc...
    fun getEditedReservation(): LiveData<MatchWithCourtAndEquipments> {
        return editedReservation
    }

    fun setEditedReservation(reservation: MatchWithCourtAndEquipments) {
        _editedReservation.value = reservation
    }

    fun setEditedMatch(match: Match) {
        _editedReservation.value?.match = match
    }

    fun setEditedEquipment(equipments: List<Equipment>) {
        _editedReservation.value?.equipments = equipments
    }

    fun setEditedPrice(finalPrice: Double) {
        _editedReservation.value?.finalPrice = finalPrice
    }

    fun fetchAvailableMatches(
        date: LocalDate,
        playerId: String,
        courtId: String,
        currentTimeslot: LocalTime
    ) {

        _listener?.remove()

        _listener = FirebaseFirestore.getInstance().collection("matches")
            .whereEqualTo("court", db.document("courts/$courtId"))
            .addSnapshotListener { matches, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val list = processMatches(matches, playerId, currentTimeslot, date)
                    _availableMatches.postValue(list.toMutableList())
                }
            }
    }

    // Add all available matches found for a given date (matches must not already contain the given player), and puts the current one at the beginning of the list
    private suspend fun processMatches(
        documents: QuerySnapshot?,
        playerId: String,
        currentTimeslot: LocalTime,
        dateToSearch: LocalDate
    ): List<Match> {

        var currentMatch: Match? = null

        documents?.documents?.mapNotNull { match ->
            if (match != null) {
                val m = firebaseToMatch(match)
                if (m.date == dateToSearch && m.time.truncatedTo(ChronoUnit.MINUTES) == currentTimeslot.truncatedTo(
                        ChronoUnit.MINUTES
                    )
                ) {
                    currentMatch = Match(m.matchId, m.numOfPlayers, m.date, m.time, m.listOfPlayers)
                    null
                } else if (m.date == dateToSearch && m.time.isAfter(LocalTime.now()) && !m.listOfPlayers.contains(
                        playerId
                    )
                ) {
                    m
                } else null
            } else null
        }.also {
            var fulledList: MutableList<Match> =
                it?.sortedBy { match -> match.time }?.toMutableList() ?: mutableListOf<Match>()
            fulledList.add(0, currentMatch!!)

            return fulledList
        }
    }

    fun submitUpdate(
        playerId: String,
        oldReservation: MatchWithCourtAndEquipments,
        callback: (Boolean) -> Unit
    ) {
        try {
            updateReservation(playerId, oldReservation)
            if (oldReservation.match.matchId != getEditedReservation().value?.match?.matchId!!) {
                updateOldMatch(playerId, oldReservation)
                updateNewMatch(playerId)
            }
            error.value = null
            callback(true)
        } catch (err: Exception) {
            error.value = err.message
            callback(false)
        }
    }

    fun cancelReservation(
        playerId: String,
        oldReservation: MatchWithCourtAndEquipments,
        callback: (Boolean) -> Unit
    ) {
        try {
            deleteReservation(oldReservation)
            updateOldMatch(playerId, oldReservation)
            error.value = null
            callback(true)
        } catch (err: Exception) {
            error.value = err.message
            callback(false)
        }
    }

    private fun updateReservation(playerId: String, oldReservation: MatchWithCourtAndEquipments) {
        db.collection("reservations").document(getEditedReservation().value?.reservationId!!).set(
            MatchWithCourtAndEquipmentsToFirebase(playerId, getEditedReservation().value!!)
        ).addOnFailureListener {
            throw Exception("Couldn't update Reservation ${oldReservation.reservationId}")
        }
    }

    private fun updateOldMatch(playerId: String, oldReservation: MatchWithCourtAndEquipments) {

        val oldMatchEdits = mutableMapOf<String, Any>()
        oldMatchEdits["numOfPlayers"] = FieldValue.increment(-1)
        Log.i("old match before", oldReservation.match.listOfPlayers.toString())
        oldReservation.match.listOfPlayers.remove(playerId)
        Log.i("old match after", oldReservation.match.listOfPlayers.toString())
        oldMatchEdits["listOfPlayers"] =
            oldReservation.match.listOfPlayers.map { db.document("players/$it") }

        // Update old match
        db.collection("matches").document(oldReservation.match.matchId)
            .update(oldMatchEdits)
            .addOnFailureListener {
                throw Exception("Couldn't update old match ${oldReservation.match.matchId}")
            }

    }

    private fun updateNewMatch(playerId: String) {
        val newMatchEdits = mutableMapOf<String, Any>()
        newMatchEdits["numOfPlayers"] = FieldValue.increment(1)
        Log.i("new match before", getEditedReservation().value?.match?.listOfPlayers!!.toString())
        getEditedReservation().value?.match?.listOfPlayers?.add(playerId)
        Log.i("new match before", getEditedReservation().value?.match?.listOfPlayers!!.toString())
        newMatchEdits["listOfPlayers"] =
            getEditedReservation().value?.match?.listOfPlayers?.map { db.document("players/$it") }!!


        // Update new match
        db.collection("matches").document(getEditedReservation().value?.match?.matchId!!)
            .update(newMatchEdits)
            .addOnFailureListener {
                throw Exception("Couldn't update new match ${getEditedReservation().value?.match?.matchId}")
            }
    }

    private fun deleteReservation(oldReservation: MatchWithCourtAndEquipments) {
        // Delete reservation
        db.collection("reservations").document(oldReservation.reservationId!!).delete()
            .addOnFailureListener {
                throw Exception("Couldn't delete reservation ${oldReservation.reservationId}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        _listener?.remove()
    }
}