package com.example.lab2.database.court_review

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CourtReviewRepository @Inject constructor(private val courtReviewDAO: CourtReviewDAO) {

    suspend fun getReviewsByCourtId(courtId: Int) =
        courtReviewDAO.getReviewsByCourtId(courtId)

    suspend fun hasReviewedMostRecentCourt(playerId: Int) =
        withContext(Dispatchers.IO){ courtReviewDAO.hasReviewedMostRecentCourt(playerId) }

    suspend fun saveReview(review: CourtReview) =
        withContext(Dispatchers.IO) { courtReviewDAO.saveReview(review) }
}