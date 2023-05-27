package com.example.lab2.viewmodels

import androidx.lifecycle.ViewModel
import com.example.lab2.entities.Sport
import com.example.lab2.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SignupVM @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun createPlayer(userId: String, name: String, surname: String, username: String, email: String, dateOfBirth: String, location: String, selectedInterests: MutableList<Sport>) {
        val user = User.toFirebase(
            User(
                full_name = "$name $surname",
                nickname = username,
                email = email,
                description = "",
                birthday = LocalDate.parse(dateOfBirth),
                address = location,
                interests = selectedInterests,
                badges = mutableMapOf(),
                image = ""
            )
        )
        db.collection("players").document(userId).set(user)
    }

    fun updatePlayer(userId: String, sports: MutableList<Sport>) {
        db.collection("players").document(userId).update("interests", sports)
    }
}