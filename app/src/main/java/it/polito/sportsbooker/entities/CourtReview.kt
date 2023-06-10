package it.polito.sportsbooker.entities

data class CourtReview(
    val courtId: String,
    val review: String,
    val ratingParameters: Map<String, Float>
)