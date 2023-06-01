package com.example.lab2.viewmodels

import androidx.lifecycle.ViewModel
import com.example.lab2.entities.BadgeType
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
                userId = userId,
                full_name = "$name $surname",
                nickname = username,
                email = email,
                description = "",
                birthday = LocalDate.parse(dateOfBirth),
                address = location,
                interests = selectedInterests,
                badges = BadgeType.values().associateWith { 0 },
                image = "https://firebasestorage.googleapis.com/v0/b/sportsbooker-mad.appspot.com/o/images%2Fprofile_picture.jpeg?alt=media&token=e5441836-e955-4a13-966b-202f0f3cd210&_gl=1*6spico*_ga*MTk2NjY0NzgxMS4xNjgzMTkzMzEy*_ga_CW55HF8NVT*MTY4NTYyMTM1MS4xNy4xLjE2ODU2MjUzMTcuMC4wLjA."
            )
        )
        db.collection("players").document(userId).set(user)
    }

    fun updatePlayer(userId: String, sports: MutableList<Sport>) {
        db.collection("players").document(userId).update("interests", sports)
    }
}