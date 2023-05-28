package com.example.lab2.viewmodels_firebase

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ReservationViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun getReservations() {
        db.collection("reservations")
    }

}