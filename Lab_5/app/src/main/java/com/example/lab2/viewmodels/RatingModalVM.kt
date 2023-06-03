package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.playerMVPVoteToFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RatingModalVM @Inject constructor(): ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _selectedMVP = MutableLiveData<String>()
    val selectedMVP: LiveData<String> = _selectedMVP
    fun setSelectedMVP(mvpUserId: String) {
        _selectedMVP.value = mvpUserId
    }

    fun submitReview(review: CourtReview) {
        db.collection("court_reviews")
            .whereEqualTo("reviewer",db.document("players/${auth.currentUser!!.uid}"))
            .whereEqualTo("court",db.document("courts/${review.courtId}"))
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection("court_reviews")
                        .add(review)
                        .addOnSuccessListener {
                            Log.d("RatingModalVM", "Review added")
                        }
                        .addOnFailureListener {
                            Log.d("RatingModalVM", "Review failed")
                        }
                } else {
                    it.documents[0].reference.update("review", review.review)
                        .addOnSuccessListener {
                            Log.d("RatingModalVM", "Review updated")
                        }
                        .addOnFailureListener {
                            Log.d("RatingModalVM", "Review update failed")
                        }
                }
            }
    }

    fun incrementMVPScore(court: Court) {
        val mvpId = _selectedMVP.value ?: return
        val sportFieldPath = "score.${court.sport}"

        val updateMap = hashMapOf<String, Any>(
            sportFieldPath to FieldValue.increment(3)
        )

        db.collection("players")
            .document(mvpId)
            .update(updateMap)
    }
}