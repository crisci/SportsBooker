package com.example.lab2.database.court

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CourtDAO {

    @Query("SELECT * FROM courts WHERE courtId = :courtId")
    fun loadCourtById(courtId: Int) : LiveData<Court>

    @Insert
    fun save(court: Court)
}