package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginRight
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lab2.calendar.CalendarVM
import com.example.lab2.calendar.NewMatchesVM
import com.example.lab2.calendar.UserViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class NewGames : Fragment(R.layout.fragment_new_games) {


    private lateinit var navController : NavController


    lateinit var vm: NewMatchesVM
    lateinit var calendarVM: CalendarVM
    @Inject
    lateinit var userVM: UserViewModel


    private lateinit var db: ReservationAppDatabase

    private lateinit var noResults: ConstraintLayout
    private lateinit var addMatchButton: FloatingActionButton

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }
    }

    private fun showOrHideNoResultImage() {
        if (vm.getMapNewMatches().value.isNullOrEmpty()) {
            noResults.visibility = View.VISIBLE
        } else {
            noResults.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = ReservationAppDatabase.getDatabase(requireContext())
        navController = findNavController()

        vm = ViewModelProvider(requireActivity())[NewMatchesVM::class.java]
        calendarVM = ViewModelProvider(requireActivity())[CalendarVM::class.java]

        vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())

        addMatchButton = view.findViewById(R.id.add_match)
        addMatchButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateMatchActivity::class.java)
            launcher.launch(intent)
        }

        val adapterCard = AdapterNewGames(emptyMap())
        adapterCard.setOnClickReservationListener(object : AdapterNewGames.OnClickReservation {
            override fun onClickReservation(bundle: Bundle) {
                val intent = Intent(requireContext(), ConfirmReservationActivity::class.java)
                intent.putExtras(bundle)
                launcher.launch(intent)
            }
        })
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapterCardFilters = AdapterFilters(listOf(null, "Padel", "Soccer", "Something"), vm::setSportFilter)
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.filters_find_game)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        noResults = view.findViewById(R.id.no_results)

        userVM.getUser().observe(viewLifecycleOwner) {
            adapterCardFilters.setFilters(listOf(null).plus(userVM.getUser().value!!.interests.map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }))
        }

        vm.getMapNewMatches().observe(requireActivity()){
            showOrHideNoResultImage()
            adapterCard.setListCourts(vm.getMapNewMatches().value!!)
        }

        calendarVM.getSelectedDate().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())
        }

        vm.getSportFilter().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())
        }

        calendarVM.getSelectedTime().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!, userVM.getUser().value!!.interests.toList())
            swipeRefreshLayout.isRefreshing = false
        }

    }
}


class ViewHolderNewGames(v: View): RecyclerView.ViewHolder(v) {

    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val timeslots: RecyclerView = v.findViewById(R.id.timeslotsRecyclerView)
    val sport_name: TextView = v.findViewById(R.id.sport_name)

}

class AdapterNewGames(private var mapNewMatches: Map<Court,List<Reservation>>): RecyclerView.Adapter<ViewHolderNewGames>(){

    private lateinit var listener: OnClickReservation

    interface OnClickReservation {
        fun onClickReservation(bundle: Bundle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNewGames {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.available_booking_card, parent, false)
        return ViewHolderNewGames(v)
    }

    override fun getItemCount(): Int {
        return mapNewMatches.size
    }

    override fun onBindViewHolder(holder: ViewHolderNewGames, position: Int) {
        val court = mapNewMatches.entries.elementAt(position).key
        val reservations = mapNewMatches.entries.elementAt(position).value.sortedBy { it.time }
        holder.timeslots.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.timeslots.adapter = TimeslotAdapter(court, reservations, object : TimeslotAdapter.OnClickTimeslot {
            override fun onClickTimeslot(timeslot: LocalTime) {
                val reservation = reservations.find { it.time == timeslot }
                val currentGameBundle = Bundle().apply {
                    putInt("reservationId", reservation!!.reservationId)
                    putString("date", reservation.date.toString())
                    putString("time", reservation.time.toString())
                    putDouble("price", reservation.price)
                    putInt("numOfPlayers", reservation.numOfPlayers)
                    putInt("courtId", reservation.courtId)
                    putString("courtName", court.name)
                    putString("sport", court.sport)
                    putInt("maxNumberOfPlayers", court.maxNumOfPlayers)
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

    fun setListCourts(newListCourts: Map<Court,List<Reservation>>) {

        val diffs = DiffUtil.calculateDiff(
            CourtTimeslotDiffCallback(mapNewMatches, newListCourts)
        )
        mapNewMatches = newListCourts
        diffs.dispatchUpdatesTo(this)
    }
}

class TimeslotAdapter(private val court: Court, private val listReservations: List<Reservation>, private val listener: OnClickTimeslot) :
    RecyclerView.Adapter<TimeslotAdapter.ViewHolder>() {

    interface OnClickTimeslot {
        fun onClickTimeslot(timeslot: LocalTime)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeslotTextView: TextView = view.findViewById(R.id.tvTimeslot)
        val numPlayersLeftTV: TextView = view.findViewById(R.id.tvPlayersLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timeslot_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = listReservations[position]
        holder.timeslotTextView.text = list.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.numPlayersLeftTV.text = "${court.maxNumOfPlayers-list.numOfPlayers} left"
        holder.itemView.setOnClickListener {
            listener.onClickTimeslot(list.time)
        }
    }

    override fun getItemCount(): Int = listReservations.size
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
