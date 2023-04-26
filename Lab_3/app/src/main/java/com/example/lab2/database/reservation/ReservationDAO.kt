package com.example.lab2.database.reservation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReservationDAO {

    @Query("SELECT * FROM reservations WHERE reservationId = (:reservationId)")
    fun loadReservationById(reservationId: Int) : LiveData<Reservation>

    @Query("SELECT * FROM reservations")
    fun loadAllReservations(): List<Reservation>

    //Not necessary to define update because of REPLACE strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReservation(reservation: Reservation)

    @Query("DELETE FROM reservations WHERE reservationId = (:reservationId)")
    fun cancelReservationById(reservationId: Int)

}