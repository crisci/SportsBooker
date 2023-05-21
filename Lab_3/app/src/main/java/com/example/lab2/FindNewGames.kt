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
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class NewGames : Fragment(R.layout.fragment_new_games), AdapterNewGames.OnClickTimeslot {


    private lateinit var navController : NavController


    lateinit var vm: NewMatchesVM
    lateinit var calendarVM: CalendarVM


    private lateinit var db: ReservationAppDatabase

    private lateinit var noResults: ConstraintLayout

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)
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

        vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)


        val adapterCard = AdapterNewGames(emptyMap(), this)
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapterCardFilters = AdapterFilters(listOf(null, "Padel", "Soccer", "Something"), vm::setSportFilter)
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.filters_find_game)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        noResults = view.findViewById(R.id.no_results)

        //vm.user.observe(viewLifecycleOwner) {
        //    adapterCardFilters.setFilters(listOf(null).plus(vm.getUser().interests.map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }))
        //}

        vm.getMapNewMatches().observe(requireActivity()){
            showOrHideNoResultImage()
            adapterCard.setListCourts(vm.getMapNewMatches().value!!)
        }

        calendarVM.getSelectedDate().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)
        }

        vm.getSportFilter().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)
        }

        calendarVM.getSelectedTime().observe(viewLifecycleOwner) {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            vm.refreshNewMatches(calendarVM.getSelectedDate().value!!, calendarVM.getSelectedTime().value!!)
            swipeRefreshLayout.isRefreshing = false
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

class AdapterNewGames(private var mapNewMatches: Map<Court,List<Reservation>>, var listener: OnClickTimeslot): RecyclerView.Adapter<ViewHolderNewGames>(){

    interface OnClickTimeslot {
        fun onClickTimeslot(informations: Bundle)
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
            val reservations = mapNewMatches.entries.elementAt(position).value
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
            CourtTimeslotDiffCallback(mapNewMatches, newListCourts)
        )
        mapNewMatches = newListCourts
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
