package com.example.lab2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.util.BuddhistCalendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.calendar.FilterViewModel
import com.example.lab2.calendar.UserViewModel
import com.example.lab2.calendar.setTextColorRes
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class NewGames : Fragment(R.layout.fragment_new_games), AdapterNewGames.OnClickTimeslot {


    private lateinit var navController : NavController
    @Inject
    lateinit var vm: CalendarViewModel

    @Inject
    lateinit var filterVM: FilterViewModel

    @Inject lateinit var userVM: UserViewModel

    private var listCourtsWithReservations = listOf<ReservationWithCourt>()
    private var mapCourtReservations = mapOf<Court,List<Reservation>>()

    private lateinit var db: ReservationAppDatabase

    private var list = listOf<ReservationWithCourt>()
    private lateinit var filteredList: List<ReservationWithCourt>

    private lateinit var noResults: ConstraintLayout

    private suspend fun getReservations() {

        listCourtsWithReservations = if (filterVM.getSportFilter() != null)
            db.reservationDao().getAvailableReservationsByDateAndSport(vm.selectedDate.value!!, filterVM.getSportFilter()!!)
        else
            db.reservationDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
                .filter { userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() }  }

        mapCourtReservations = listCourtsWithReservations
            .groupBy({ it.court }, { it.reservation })
            .mapValues { it.value.toList() }
        showOrHideNoResultImage()
        vm.mapCourtReservations.postValue(mapCourtReservations)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            CoroutineScope(Dispatchers.IO).launch {
                getReservations()
            }
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }
    }

    private suspend fun showOrHideNoResultImage() {
        withContext(Dispatchers.Main) {
            if (listCourtsWithReservations.isNotEmpty()) {
                noResults.visibility = View.GONE
            } else {
                noResults.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = ReservationAppDatabase.getDatabase(requireContext())
        navController = findNavController()


        val adapterCard = AdapterNewGames(mapCourtReservations, this)
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapterCardFilters = AdapterFilters(listOf(null, "Padel", "Soccer", "Something"), filterVM::setSportFilter)
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.filters_find_game)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        noResults = view.findViewById(R.id.no_results)


        userVM.user.observe(viewLifecycleOwner) {
            adapterCardFilters.setFilters(listOf(null).plus(userVM.getUser().interests.map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }))
        }

        vm.mapCourtReservations.observe(requireActivity()){
            adapterCard.setListCourts(vm.mapCourtReservations.value!!)
        }

        vm.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                getReservations()
            }
        }

        filterVM.sportFilter.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                getReservations()
            }
        }

    }

    override fun onClickTimeslot(informations: Bundle) {
        val intentConfirmReservationActivity = Intent(activity, ConfirmReservationActivity::class.java).putExtras(informations)
        launcher.launch(intentConfirmReservationActivity)
    }

}


class ViewHolderNewGames(v: View): RecyclerView.ViewHolder(v) {

    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val timeslots: GridLayout = v.findViewById(R.id.timeslots)
    val sport_name: TextView = v.findViewById(R.id.sport_name)
}

class AdapterNewGames(private var mapCourtReservations: Map<Court,List<Reservation>>, var listener: OnClickTimeslot): RecyclerView.Adapter<ViewHolderNewGames>(){

    interface OnClickTimeslot {
        fun onClickTimeslot(informations: Bundle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNewGames {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.available_booking_card, parent, false)
        return ViewHolderNewGames(v)
    }

    override fun getItemCount(): Int {
        return mapCourtReservations.size
    }

    override fun onBindViewHolder(holder: ViewHolderNewGames, position: Int) {
            val court = mapCourtReservations.entries.elementAt(position).key
            val reservations = mapCourtReservations.entries.elementAt(position).value
            val timeSlots = reservations.map { it.time }

            holder.timeslots.removeAllViews()

            for (t in timeSlots) {
                val tv = TextView(holder.itemView.context)
                tv.text = "${t.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                val layoutParams = LinearLayout.LayoutParams(
                    220,
                    100
                )
                layoutParams.setMargins(10, 0, 20, 20)
                tv.setPadding(20,25,20,25)
                tv.setTextAppearance(R.style.TimeslotTextAppearance)
                tv.elevation = 10F
                tv.background = holder.itemView.context.getDrawable(R.drawable.timeslot)
                tv.textAlignment = View.TEXT_ALIGNMENT_CENTER;
                tv.id = View.generateViewId()
             tv.setOnClickListener {
                    val currentGameBundle = Bundle().apply {
                        putInt("reservationId", reservations[timeSlots.indexOf(t)].reservationId)
                        putString("date", reservations[timeSlots.indexOf(t)].date.toString())
                        putString("time", reservations[timeSlots.indexOf(t)].time.toString())
                        putDouble("price", reservations[timeSlots.indexOf(t)].price)
                        putInt("numOfPlayers", reservations[timeSlots.indexOf(t)].numOfPlayers)
                        putInt("courtId", reservations[timeSlots.indexOf(t)].courtId)
                        putString("courtName", court.name)
                        putString("sport", court.sport)
                        putInt("maxNumberOfPlayers", court.maxNumOfPlayers)
                    }
                    listener.onClickTimeslot(currentGameBundle)
                }
                holder.timeslots.addView(tv, layoutParams)
            }
        holder.name.text = "${court.name}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.sport_name.text = "${court.sport}"
    }

    fun setListCourts(newListCourts: Map<Court,List<Reservation>>) {

        val diffs = DiffUtil.calculateDiff(
            CourtTimeslotDiffCallback(mapCourtReservations, newListCourts)
        )
        mapCourtReservations = newListCourts
        diffs.dispatchUpdatesTo(this)
    }
}

class ViewHolderFilter(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.filter_name)
    val layout: ConstraintLayout = v.findViewById(R.id.filter_button_layout)
    val selectionIndicator: View = v.findViewById(R.id.selectionIndicator)
}

class AdapterFilters(private var listOfSport: List<String?>, val setFilter: (input: String?) -> Unit): RecyclerView.Adapter<ViewHolderFilter>(){

    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFilter {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_button, parent, false)
        return ViewHolderFilter(v)
    }

    override fun getItemCount(): Int = listOfSport.size

    override fun onBindViewHolder(holder: ViewHolderFilter, position: Int) {
        val name = listOfSport[position]

        holder.selectionIndicator.visibility = if (selectedPosition == position) View.VISIBLE else View.INVISIBLE

        holder.name.text = name?:"All"
        holder.layout.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            setFilter(name)
        }
    }

    fun setFilters(newFilters: List<String?>) {

        val diffs = DiffUtil.calculateDiff(
            FilterDiffCallback(listOfSport, newFilters)
        )
        listOfSport = newFilters
        diffs.dispatchUpdatesTo(this)
    }

}
