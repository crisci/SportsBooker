package com.example.lab2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.calendar.setTextColorRes
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class NewGames : Fragment(R.layout.fragment_new_games), AdapterNewGames.OnClickTimeslot {

    private lateinit var navController : NavController
    @Inject
    lateinit var vm: CalendarViewModel

    private var listCourtsWithReservations = listOf<CourtWithReservations>()

    private lateinit var db: ReservationAppDatabase
    private var mapCourtsWithAvailableTimeslots = mapOf<Court,Set<LocalTime>>()


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            CoroutineScope(Dispatchers.IO).launch {
                listCourtsWithReservations = db.courtDao().getReservationsByDate(vm.selectedDate.value!!)
                mapCourtsWithAvailableTimeslots = computeAvailableTimeslotsForAllCourts(listCourtsWithReservations)
                Log.d("mapCourtsWithAvailableTimeslots",mapCourtsWithAvailableTimeslots.toString())
                vm.mapCourtsWithAvailableTimeslots.postValue(mapCourtsWithAvailableTimeslots)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = ReservationAppDatabase.getDatabase(requireContext())
        mapCourtsWithAvailableTimeslots = emptyMap()
        navController = findNavController()

        val adapterCard = AdapterNewGames(mapCourtsWithAvailableTimeslots, this)
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

/*        CoroutineScope(Dispatchers.IO).launch {
            db.reservationDao().saveReservation(
                Reservation(
                    0,
                    Random.nextInt(1, 3),
                    Random.nextInt(1, 7),
                    7.00,
                    LocalDate.now(),
                    LocalTime.of(Random.nextInt(8, 24),Random.nextInt(0, 60))
                )
            )
        }*/

        vm.mapCourtsWithAvailableTimeslots.observe(requireActivity()){
            adapterCard.setListCourts(vm.mapCourtsWithAvailableTimeslots.value!!)
        }

        vm.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                listCourtsWithReservations = db.courtDao().getReservationsByDate(vm.selectedDate.value!!)
                mapCourtsWithAvailableTimeslots = computeAvailableTimeslotsForAllCourts(listCourtsWithReservations)
                Log.d("mapCourtsWithAvailableTimeslots",mapCourtsWithAvailableTimeslots.toString())
                vm.mapCourtsWithAvailableTimeslots.postValue(mapCourtsWithAvailableTimeslots)
            }
        }
    }

    // Define a function to compute all available timeslots for all courts
    private fun computeAvailableTimeslotsForAllCourts(listCourtsWithReservations: List<CourtWithReservations>): Map<Court, Set<LocalTime>> {
        // create a set of all available timeslots
        val allTimeslots = setOf(
            LocalTime.of(8, 30),
            LocalTime.of(10, 0),
            LocalTime.of(11, 30),
            LocalTime.of(13, 0),
            LocalTime.of(14, 30),
            LocalTime.of(16, 0),
            LocalTime.of(17,30),
            LocalTime.of(19, 0),
            LocalTime.of(20, 30),
            LocalTime.of(22, 0),
        )

        // create a map to store the available timeslots for each court
        val availableTimeslotsByCourt = mutableMapOf<Court, Set<LocalTime>>()

        // loop through each court with reservations
        for (courtWithReservations in listCourtsWithReservations) {
            // get the set of reserved timeslots for the court
            val reservedTimeslots = courtWithReservations.reservations.map { it.time }

            // subtract the reserved timeslots from the set of all timeslots to get the available timeslots
            val availableTimeslots = allTimeslots - reservedTimeslots.toSet()

            // add the court and available timeslots to the map
            availableTimeslotsByCourt[courtWithReservations.court] = availableTimeslots
        }

        // return the map of available timeslots by court
        return availableTimeslotsByCourt
    }

    // TODO
    override fun onClickTimeslot() {
    }


}

class ViewHolderNewGames(v: View): RecyclerView.ViewHolder(v) {

    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val timeslots: GridLayout = v.findViewById(R.id.timeslots)
}

class AdapterNewGames(private var mapCourtsWithAvailableTimeslots: Map<Court,Set<LocalTime>>, var listener: OnClickTimeslot): RecyclerView.Adapter<ViewHolderNewGames>(){

    interface OnClickTimeslot {
        fun onClickTimeslot()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNewGames {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.available_booking_card, parent, false)
        return ViewHolderNewGames(v)
    }

    override fun getItemCount(): Int {
        return mapCourtsWithAvailableTimeslots.size
    }

    override fun onBindViewHolder(holder: ViewHolderNewGames, position: Int) {
            val court = mapCourtsWithAvailableTimeslots.keys.elementAt(position)
            val timeSlots = mapCourtsWithAvailableTimeslots[court] ?: emptySet()
            for (t in timeSlots) {
                val tv = TextView(holder.itemView.context)
                tv.text = "${t.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                val layoutParams = LinearLayout.LayoutParams(
                    220,
                    100
                )
                layoutParams.setMargins(0, 0, 20, 20)
                tv.setPadding(20,25,20,25)
                tv.setTextAppearance(R.style.TimeslotTextAppearance)
                tv.setTextColorRes(R.color.timeslot)
                tv.elevation = 5F
                tv.background = holder.itemView.context.getDrawable(R.drawable.timeslot)
                tv.textAlignment = View.TEXT_ALIGNMENT_CENTER;
                tv.id = View.generateViewId()
                // TODO When the user presses a timeslot, a window ConfirmBooking will open
                tv.setOnClickListener {
                    listener.onClickTimeslot()
                }
                holder.timeslots.addView(tv, layoutParams)
            }
        holder.name.text = "Campo ${court.courtId}"
        holder.location.text = "Via Giovanni Magni, 32"
    }

    fun setListCourts(newListCourts: Map<Court,Set<LocalTime>>) {

        val diffs = DiffUtil.calculateDiff(
            CourtTimeslotDiffCallback(mapCourtsWithAvailableTimeslots, newListCourts)
        )
        mapCourtsWithAvailableTimeslots = newListCourts
        diffs.dispatchUpdatesTo(this)
    }
}