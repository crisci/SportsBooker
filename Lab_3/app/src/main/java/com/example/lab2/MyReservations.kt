package com.example.lab2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.calendar.setTextColorRes
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.formatPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MyReservations : Fragment(R.layout.fragment_my_reservations), AdapterCard.OnEditClickListener  {

    private lateinit var navController : NavController
    @Inject
    lateinit var calendar: CalendarViewModel

    private var list = listOf<Reservation>()

    private lateinit var db: ReservationAppDatabase
    private lateinit var filteredList: List<Reservation>

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            CoroutineScope(Dispatchers.IO).launch {
                list = db.reservationDao().loadAllReservations()
                filteredList = list.filter { it.date.dayOfYear == calendar.selectedDate.value?.dayOfYear }
                calendar.list.postValue(filteredList)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = ReservationAppDatabase.getDatabase(requireContext())
        filteredList = emptyList()
        navController = findNavController()

        val adapterCard = AdapterCard(filteredList, this )
        val recyclerViewCard = view.findViewById<RecyclerView>(R.id.your_reservation_recycler_view)
        recyclerViewCard.adapter = adapterCard
        recyclerViewCard.layoutManager = LinearLayoutManager(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            db.reservationDao().saveReservation(
                Reservation(
                    0,
                    1,
                    3,
                    7.0,
                    LocalDate.now(),
                    LocalTime.of(11,0)
                )
            )
            db.reservationDao().saveReservation(
                Reservation(
                    0,
                    1,
                    3,
                    7.0,
                    LocalDate.now(),
                    LocalTime.of(12,0)
                )
            )
        }

        calendar.list.observe(requireActivity()){
            adapterCard.setReservations(calendar.list.value!!)
        }

        calendar.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                list = db.reservationDao().loadAllReservations()
                filteredList = list.filter { it.date.dayOfYear == calendar.selectedDate.value?.dayOfYear }
                calendar.list.postValue(filteredList)
            }
        }
    }



    override fun onEditClick(reservation: Reservation) {
        val intentEditReservation = Intent(activity, EditReservationActivity::class.java).apply {
            addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
            putExtra("reservationId", reservation.reservationId)
            putExtra("courtId", reservation.courtId)
            putExtra("date", reservation.date.toString())
            putExtra("time", reservation.time.toString())
            putExtra("price", reservation.price)
            putExtra("numOfPlayers", reservation.numOfPlayers)
        }
        launcher.launch(intentEditReservation)
    }

}

class ViewHolderCard(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val price: TextView = v.findViewById(R.id.price_reservation)
    val currentNumberOfPlayers: TextView = v.findViewById(R.id.current_number_of_players)
    val maxNumberOfPlayers: TextView = v.findViewById(R.id.max_number_players)
    val time: TextView = v.findViewById(R.id.time_reservation)
    val editButton : ImageButton = v.findViewById(R.id.edit_reservation_button)
}

class AdapterCard(private var list: List<Reservation>, private val listener: OnEditClickListener): RecyclerView.Adapter<ViewHolderCard>() {

    interface OnEditClickListener {
        fun onEditClick(reservation: Reservation)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCard {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.reservation_card_layout, parent, false)
        return ViewHolderCard(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolderCard, position: Int) {
        val maxNumPlayers = 7
        holder.name.text = "Campo ${list[position].courtId}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.price.text = "€ ${list[position].formatPrice()}"
        if(list[position].numOfPlayers == maxNumPlayers) {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.example_1_bg)
        }
        else {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.bright_red)
        }
        holder.currentNumberOfPlayers.text = "${list[position].numOfPlayers}"
        holder.maxNumberOfPlayers.text = "/7"
        holder.time.text = list[position].time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()

        holder.editButton.setOnClickListener { listener.onEditClick(list[position]) }
    }

    fun setReservations(newReservations: List<Reservation>) {
        val diffs = DiffUtil.calculateDiff(
            ReservationDiffCallback(list, newReservations))
        list = newReservations
        diffs.dispatchUpdatesTo(this)
    }

}