package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipmentsToFirebase
import com.example.lab2.viewmodels_firebase.toTimestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ConfirmReservationVM @Inject constructor(): ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> = _exceptionMessage

    fun addReservation(newMatch: MatchWithCourtAndEquipments) {
        viewModelScope.launch {
            val startTimestamp = LocalDateTime.of(newMatch.match.date,newMatch.match.time).minusMinutes(1).toTimestamp()
            val endTimestamp = LocalDateTime.of(newMatch.match.date,newMatch.match.time).plusMinutes(1).toTimestamp()
            val playerRef = db.collection("players").document(auth.currentUser!!.uid)
            val playerMatchQuery = db.collection("matches")
                .whereArrayContains("listOfPlayers", playerRef)
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)

            playerMatchQuery.get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    db.collection("reservations").add(
                        MatchWithCourtAndEquipmentsToFirebase(
                            auth.currentUser!!.uid,
                            newMatch
                        )
                    ).addOnSuccessListener { reservationDocRef ->
                        val matchRef = db.collection("matches").document(newMatch.match.matchId!!)
                        matchRef.update("listOfPlayers", FieldValue.arrayUnion(playerRef))
                        matchRef.update("numOfPlayers", FieldValue.increment(1))
                    }.addOnFailureListener { exception ->
                        _exceptionMessage.postValue("Couldn't add Reservation")
                        throw exception
                    }
                } else {
                    // Player already has a match at the specified timestamp
                    _exceptionMessage.postValue("Player already has a match at that timestamp")
                }
            }.addOnFailureListener { exception ->
                _exceptionMessage.postValue("Couldn't check player's matches")
                throw exception
            }
        }
    }
}