package com.example.lab2.reservation.book_reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.R
import com.example.lab2.entities.Court
import com.example.lab2.entities.Match
import com.example.lab2.entities.MatchWithCourt
import kotlinx.serialization.json.Json
import java.time.LocalTime

class AdapterRVListMatches(private var mapNewMatches: Map<Court, List<Match>>) :
    RecyclerView.Adapter<AdapterRVListMatches.ViewHolderBookReservation>() {

    private lateinit var listener: OnClickReservation

    interface OnClickReservation {
        fun onClickReservation(bundle: Bundle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBookReservation {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.available_booking_card, parent, false)
        return ViewHolderBookReservation(v)
    }

    override fun getItemCount(): Int {
        return mapNewMatches.size
    }

    override fun onBindViewHolder(holder: ViewHolderBookReservation, position: Int) {
        val court = mapNewMatches.entries.elementAt(position).key
        val matches = mapNewMatches.entries.elementAt(position).value
        holder.timeslots.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.timeslots.adapter =
            AdapterRVTimeslots(court, matches, object : AdapterRVTimeslots.OnClickTimeslot {
                override fun onClickTimeslot(timeslot: LocalTime) {
                    val match = matches.find { it.time == timeslot }
                    val currentGameBundle = Bundle().apply {
                        val matchWithCourt = MatchWithCourt(match!!, court)
                        val jsonString =
                            Json.encodeToString(MatchWithCourt.serializer(), matchWithCourt)
                        putString("jsonMatch", jsonString)
                    }

                    listener.onClickReservation(currentGameBundle)
                }
            })

        holder.name.text = "${court.name}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.sport_name.text = "${court.sport}"
    }

    fun setOnClickReservationListener(listener: OnClickReservation) {
        this.listener = listener
    }

    fun setListCourts(newListCourts: Map<Court, List<Match>>) {

        val diffs = DiffUtil.calculateDiff(
            CourtTimeslotDiffCallback(mapNewMatches, newListCourts)
        )
        mapNewMatches = newListCourts
        diffs.dispatchUpdatesTo(this)
    }

    inner class ViewHolderBookReservation(v: View) : RecyclerView.ViewHolder(v) {

        val name: TextView = v.findViewById(R.id.court_name_reservation)
        val location: TextView = v.findViewById(R.id.location_reservation)
        val timeslots: RecyclerView = v.findViewById(R.id.timeslotsRecyclerView)
        val sport_name: TextView = v.findViewById(R.id.sport_name)

    }
}