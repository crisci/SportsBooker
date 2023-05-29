package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.database.reservation.formatPrice
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments

class ReservationDiffCallback(
    private  val reservations: List<MatchWithCourtAndEquipments>,
    private val newReservations: List<MatchWithCourtAndEquipments>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = reservations.size

    override fun getNewListSize(): Int = newReservations.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return reservations[oldItemPosition].match.matchId == newReservations[newItemPosition].match.matchId
                && reservations[oldItemPosition].equipments == newReservations[newItemPosition].equipments
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val (_, courtId, numOfPlayers, price, date, time) = reservations[oldItemPosition].court
        val (_, courtId1, numOfPlayers1, price1, date1, time1) = newReservations[newItemPosition].court
        return  courtId == courtId1 && numOfPlayers == numOfPlayers1 && price == price1 && date == date1 && time == time1
    }


}