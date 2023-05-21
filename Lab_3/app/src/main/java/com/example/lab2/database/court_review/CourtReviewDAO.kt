package com.example.lab2.database.court_review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.lab2.database.court.Court
import java.time.LocalDate

@Dao
interface CourtReviewDAO {

    @Query("SELECT * FROM courts_reviews WHERE courtId = :courtId")
    fun getReviewsByCourtId(courtId: Int): CourtReview

    @Query("SELECT c.*" +
            " FROM reservations r" +
            " JOIN players_reservations pr ON pr.reservationId = r.reservationId" +
            " JOIN courts c ON c.courtId = r.courtId" +
            " WHERE pr.playerId = :playerId" +
            " AND r.date < :date" +
            " AND NOT EXISTS  (" +
            "  SELECT 1" +
            "  FROM courts_reviews cr" +
            "  WHERE cr.playerId = :playerId" +
            "  AND cr.courtId = c.courtId" +
            ")")
    fun hasReviewedMostRecentCourt(playerId: Int, date: LocalDate = LocalDate.now()): Court?

    @Insert
    fun saveReview(review: CourtReview)
}