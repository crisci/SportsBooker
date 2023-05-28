package com.example.lab2.viewmodels_firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Singleton

@Singleton
class ReservationViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _myReservations = MutableLiveData<List<ReservationWithCourtAndEquipments>>()
    val myReservations: LiveData<List<ReservationWithCourtAndEquipments>> = _myReservations




    fun getPlayerReservations(playerId: String) {
        db.collection("reservations")
            .whereEqualTo("player",db.collection("players").document(playerId))
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val reservationId = document.id
                    val matchRef = document.getDocumentReference("match")
                    Log.e("matchRef", matchRef?.id.toString())

                    // Retrieve the reservation details
                    matchRef?.get()
                        ?.addOnSuccessListener { matchSnapshot ->
                            val courtRef = matchSnapshot.getDocumentReference("court")
                            val numOfPlayers = matchSnapshot.getLong("numOfPlayers")
                            val date = matchSnapshot.getString("date")
                            val time = matchSnapshot.getString("time")

                            // Retrieve the court details
                            courtRef?.get()
                                ?.addOnSuccessListener { courtSnapshot ->
                                    val courtName = courtSnapshot.getString("name")
                                    val location = courtSnapshot.getString("location")
                                    val maxNumOfPlayers = courtSnapshot.getLong("maxNumOfPlayers")

                                    // Retrieve the finalPrice from the reservation
                                    val finalPrice = document.getDouble("finalPrice")

                                    // Print or process the retrieved information
                                    Log.d("ResViewModel","Reservation: $reservationId Court Name: $courtName Location: $location Number of Players: $numOfPlayers date: $date, Time: $time")
                                }
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Error getting player reservations: $exception")
            }
    }
}