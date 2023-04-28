package com.example.lab2

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import java.time.LocalTime

class CourtTimeslotDiffCallback(
    private  val listCourts: Map<Court,Set<LocalTime>>,
    private val newListCourts: Map<Court,Set<LocalTime>>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = listCourts.keys.size

    override fun getNewListSize(): Int = newListCourts.keys.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listCourts.entries.elementAt(oldItemPosition).key.courtId == newListCourts.entries.elementAt(oldItemPosition).key.courtId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEntry = listCourts.entries.elementAt(oldItemPosition)
        val newEntry = newListCourts.entries.elementAt(newItemPosition)
        return oldEntry == newEntry
    }
}