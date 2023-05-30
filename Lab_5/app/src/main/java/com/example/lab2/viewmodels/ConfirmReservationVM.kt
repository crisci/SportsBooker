package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipmentsToFirebase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmReservationVM @Inject constructor(): ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun addReservation(playerId: String, newMatch: MatchWithCourtAndEquipments){
        viewModelScope.launch {
            db.collection("reservations").add(
                MatchWithCourtAndEquipmentsToFirebase(
                    playerId,
                    newMatch
                )
            )
                .addOnSuccessListener {
                    db.collection("matches").document(newMatch.match.matchId)
                        .update("listOfPlayers", newMatch.match.listOfPlayers)
                }
                .addOnFailureListener {
                Log.i("update", "Couldn't add Reservation")
                throw it
            }
        }
    }
}