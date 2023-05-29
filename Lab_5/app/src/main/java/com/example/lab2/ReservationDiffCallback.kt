package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.database.reservation.formatPrice
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.ReservationViewModel

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
        return areCourtTheSame(reservations[oldItemPosition].court, newReservations[newItemPosition].court) &&
                areMatchTheSame(reservations[oldItemPosition].match, newReservations[newItemPosition].match) &&
                areReservationTheSame(reservations[oldItemPosition], newReservations[newItemPosition])

    }

    private fun areCourtTheSame(oldCourt: Court, newCourt: Court): Boolean {
        return oldCourt.courtId == newCourt.courtId &&
                oldCourt.description == newCourt.description &&
                oldCourt.image == newCourt.image &&
                oldCourt.maxNumberOfPlayers == newCourt.maxNumberOfPlayers &&
                oldCourt.name == newCourt.name &&
                oldCourt.sport == newCourt.sport
    }

    private fun areMatchTheSame(oldMatch: Match, newMatch: Match): Boolean {
        return oldMatch.matchId == newMatch.matchId &&
                oldMatch.date == newMatch.date &&
                oldMatch.time == newMatch.time &&
                oldMatch.numOfPlayers == newMatch.numOfPlayers
    }

    private fun areReservationTheSame(oldReservation: MatchWithCourtAndEquipments, newReservation: MatchWithCourtAndEquipments): Boolean {
        return oldReservation.reservationId == newReservation.reservationId &&
                oldReservation.finalPrice == newReservation.finalPrice
    }


}