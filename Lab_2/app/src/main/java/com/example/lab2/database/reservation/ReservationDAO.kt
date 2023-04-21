package com.example.lab2.database.reservation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ReservationDAO {

    @Query("SELECT * FROM reservations WHERE reservationId = (:reservationId)")
    fun loadReservationById(reservationId: Int) : LiveData<Reservation>
}