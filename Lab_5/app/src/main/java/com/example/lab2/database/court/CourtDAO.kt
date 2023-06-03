package com.example.lab2.database.court

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface CourtDAO {

    @Query("SELECT * FROM courts WHERE courtId = :courtId")
    fun loadCourtById(courtId: Int): LiveData<Court>

    @Query("SELECT * FROM courts")
    fun loadAllCourts(): List<Court>

    @Insert
    fun save(court: Court)

    @Query(
        "SELECT courts.courtId " +
                "FROM courts WHERE courts.courtId NOT IN " +
                "(SELECT reservations.courtId FROM reservations WHERE date = :date AND time = :time) " +
                "AND courts.sport = :sport " +
                "LIMIT 1"
    )
    fun getFirstAvailableCourtForSportDateTime(sport: String, date: LocalDate, time: LocalTime): Int

}
