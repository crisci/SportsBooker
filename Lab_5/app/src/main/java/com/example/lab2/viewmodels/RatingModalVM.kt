package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.Court
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.database.court_review.CourtReviewRepository
import com.example.lab2.database.player.Player
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.MatchToReview
import com.example.lab2.viewmodels_firebase.firebaseToCourt
import com.example.lab2.viewmodels_firebase.firebaseToMatch
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RatingModalVM @Inject constructor(
    private val courtReviewRepository: CourtReviewRepository,
): ViewModel() {

    private val db = FirebaseFirestore.getInstance()

/*    fun submitReview(review: CourtReview) {
        viewModelScope.launch {
            courtReviewRepository.saveReview(review)
            showBanner.value = false
        }
    }*/
}