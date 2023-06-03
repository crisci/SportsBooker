package com.example.lab2.database.court_review

data class CourtReview(
    val courtId: String,
    val review: String,
    val ratingParameters: Map<String, Float>
)