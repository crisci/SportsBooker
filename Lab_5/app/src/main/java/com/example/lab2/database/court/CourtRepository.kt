package com.example.lab2.database.court

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class CourtRepository @Inject constructor(private val database: CourtDAO) {

    suspend fun loadAllCourts() = database.loadAllCourts()

    suspend fun loadCourtById(courtId: Int) = database.loadCourtById(courtId)

    suspend fun save(court: Court) = database.save(court)

    suspend fun getFirstAvailableCourtForSportDateTime(
        sport: String,
        date: LocalDate,
        time: LocalTime
    ) =
        withContext(Dispatchers.IO) {
            database.getFirstAvailableCourtForSportDateTime(sport, date, time)
        }
}