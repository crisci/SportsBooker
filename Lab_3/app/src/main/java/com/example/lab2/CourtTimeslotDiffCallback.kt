package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.Reservation
import java.time.LocalTime

class CourtTimeslotDiffCallback(
    private  val listCourts: List<CourtWithReservations>,
    private val newListCourts: List<CourtWithReservations>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = listCourts.size

    override fun getNewListSize(): Int = newListCourts.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listCourts[oldItemPosition].court.courtId == newListCourts[newItemPosition].court.courtId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEntry = listCourts[oldItemPosition]
        val newEntry = newListCourts[newItemPosition]
        return oldEntry == newEntry
    }
}