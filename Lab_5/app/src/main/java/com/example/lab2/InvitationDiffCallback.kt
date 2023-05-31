package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.database.reservation.formatPrice
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.ReservationViewModel

class InvitationDiffCallback(
    private  val invitations: List<Invitation>,
    private val newInvitations: List<Invitation>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = invitations.size

    override fun getNewListSize(): Int = newInvitations.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return invitations[oldItemPosition].sender == newInvitations[newItemPosition].sender
                && invitations[oldItemPosition].date == newInvitations[newItemPosition].date
                && invitations[oldItemPosition].time == newInvitations[newItemPosition].time
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return isCourtTheSame(invitations[oldItemPosition].court, newInvitations[newItemPosition].court) &&
                isMatchTheSame(invitations[oldItemPosition].match, newInvitations[newItemPosition].match) &&
                isInvitationTheSame(invitations[oldItemPosition], newInvitations[newItemPosition])

    }

    private fun isCourtTheSame(oldCourt: Court, newCourt: Court): Boolean {
        return oldCourt.courtId == newCourt.courtId &&
                oldCourt.description == newCourt.description &&
                oldCourt.image == newCourt.image &&
                oldCourt.maxNumberOfPlayers == newCourt.maxNumberOfPlayers &&
                oldCourt.name == newCourt.name &&
                oldCourt.sport == newCourt.sport
    }

    private fun isMatchTheSame(oldMatch: Match, newMatch: Match): Boolean {
        return oldMatch.matchId == newMatch.matchId &&
                oldMatch.date == newMatch.date &&
                oldMatch.time == newMatch.time &&
                oldMatch.numOfPlayers == newMatch.numOfPlayers
    }

    private fun isInvitationTheSame(oldInvitation: Invitation, newInvitation: Invitation): Boolean {
        return oldInvitation.sender == newInvitation.sender &&
                oldInvitation.date == newInvitation.date &&
                oldInvitation.time == newInvitation.time
    }

}