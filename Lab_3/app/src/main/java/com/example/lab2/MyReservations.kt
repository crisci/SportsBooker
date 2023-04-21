package com.example.lab2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.calendar.WeekCalendar
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.formatPrice
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MyReservations : Fragment(R.layout.fragment_my_reservations) {

    private lateinit var navController : NavController
    @Inject
    lateinit var calendar: CalendarViewModel

    private val list = listOf<Reservation>(
        Reservation(1,1,3,7.00, LocalDate.now().plusDays(1), LocalTime.now()),
        Reservation(1,1,3,7.00, LocalDate.now().plusDays(2), LocalTime.now()),
        Reservation(1,1,3,7.00, LocalDate.now(), LocalTime.now()),
        Reservation(1,1,3,7.00, LocalDate.now(), LocalTime.now()),
        Reservation(1,1,3,7.00, LocalDate.now(), LocalTime.now()),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var filteredList = list

        navController = findNavController()

        //calendar = ViewModelProvider(this)[CalendarViewModel::class.java]

        val adapterCard = AdapterCard(filteredList)

        val recyclerViewCard = view.findViewById<RecyclerView>(R.id.your_reservation_recycler_view)
        recyclerViewCard.adapter = adapterCard
        recyclerViewCard.layoutManager = LinearLayoutManager(requireContext())

        calendar.selectedDate().observe(viewLifecycleOwner) {
            Log.e("Date", "Reservation: ${calendar.selectedDate().value.toString()}")
            filteredList = list.filter { it.date.dayOfYear == calendar.selectedDate().value?.dayOfYear }
            Log.e("calendar", "${calendar.selectedDate().value}: ${filteredList.size}")
            adapterCard.setReservations(filteredList)
        }

    }

}

class ViewHolderCard(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val price: TextView = v.findViewById(R.id.price_reservation)
    val nPlayersLeft: TextView = v.findViewById(R.id.number_players)
    val time: TextView = v.findViewById(R.id.time_reservation)
}

class AdapterCard(private var list: List<Reservation>): RecyclerView.Adapter<ViewHolderCard>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCard {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.reservation_card_layout, parent, false)
        return ViewHolderCard(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolderCard, position: Int) {
        holder.name.text = "Campo ${list[position].courtId}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.price.text = "€ ${list[position].formatPrice()}"
        holder.nPlayersLeft.text = "${list[position].numOfPlayers} left"
        holder.time.text = list[position].time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }

    fun setReservations(newReservations: List<Reservation>) {
        val diffs = DiffUtil.calculateDiff(
            ReservationDiffCallback(list, newReservations))
        list = newReservations
        diffs.dispatchUpdatesTo(this)
    }

}