package com.example.lab2.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.database.court.Court
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.database.court_review.CourtReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingModalVM @Inject constructor(
    private val courtReviewRepository: CourtReviewRepository,
): ViewModel() {

    private val _courtToReview = MutableLiveData<Court?>()
    private val courtToReview: LiveData<Court?> = _courtToReview
    fun getCourtToReview() : LiveData<Court?> {
        return courtToReview
    }

    fun checkIfPlayerHasAlreadyReviewed(playerId: Int) {
        viewModelScope.launch {
            val court = courtReviewRepository.hasReviewedMostRecentCourt(playerId)
            if (court != null) {
                _courtToReview.value = court
            }
        }
    }

    fun submitReview(review: CourtReview) {
        viewModelScope.launch {
            courtReviewRepository.saveReview(review)
        }
    }
}