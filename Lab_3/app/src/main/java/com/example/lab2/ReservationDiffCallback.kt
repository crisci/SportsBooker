package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.reservation.Reservation

class ReservationDiffCallback(
    private  val reservations: List<Reservation>,
    private val newReservations: List<Reservation>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = reservations.size

    override fun getNewListSize(): Int = newReservations.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return reservations[oldItemPosition].reservationId == newReservations[newItemPosition].reservationId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        Log.d("OldRes: ",reservations[oldItemPosition].toString())
        Log.d("NewRes: ",newReservations[newItemPosition].toString())
        val (_, courtId, numOfPlayers, price, date, time) = reservations[oldItemPosition]
        val (_, courtId1, numOfPlayers1, price1, date1, time1) = newReservations[newItemPosition]
        return  courtId == courtId1 && numOfPlayers == numOfPlayers1 && price == price1 && date == date1 && time == time1
    }


}