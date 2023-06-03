package com.example.lab2.entities.database

data class CourtReview(
    val courtId: String,
    val review: String,
    val ratingParameters: Map<String, Float>
)