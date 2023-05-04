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
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.ReservationWithCourt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private var listCourtsWithReservations = listOf<CourtWithReservations>()

    private lateinit var db: ReservationAppDatabase

    private var list = listOf<ReservationWithCourt>()
    private lateinit var filteredList: List<ReservationWithCourt>

    private lateinit var selectedFilterName: TextView


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            CoroutineScope(Dispatchers.IO).launch {
               listCourtsWithReservations = db.courtDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
                listCourtsWithReservations = if(filterVM.getSportFilter() != null) listCourtsWithReservations.filter { it.court.sport == filterVM.getSportFilter() } else listCourtsWithReservations.filter { userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() }  }
               vm.listAvailableReservations.postValue(listCourtsWithReservations)
            }
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = ReservationAppDatabase.getDatabase(requireContext())
        navController = findNavController()

        selectedFilterName = view.findViewById(R.id.selected_filter)
        selectedFilterName.text = filterVM.getSportFilter()?:"All"

        val adapterCard = AdapterNewGames(listCourtsWithReservations, this)
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapterCardFilters = AdapterFilters(listOf(null, "Padel", "Soccer", "Something"), filterVM::setSportFilter)
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.filters_find_game)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);


        userVM.user.observe(viewLifecycleOwner) {
            adapterCardFilters.setFilters(listOf(null).plus(userVM.getUser().interests.map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }))
        }

        vm.listAvailableReservations.observe(requireActivity()){
            adapterCard.setListCourts(vm.listAvailableReservations.value!!)
        }

        vm.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                listCourtsWithReservations = db.courtDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
                filterVM.sportFilter.postValue(null)
                listCourtsWithReservations = if(filterVM.getSportFilter() != null) listCourtsWithReservations.filter { it.court.sport == filterVM.getSportFilter() } else listCourtsWithReservations.filter { userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() }  }
                Log.e("list", listCourtsWithReservations.toString())
                vm.listAvailableReservations.postValue(listCourtsWithReservations)
            }
        }

        filterVM.sportFilter.observe(viewLifecycleOwner) {
            selectedFilterName.text = filterVM.getSportFilter()?:"All"
            CoroutineScope(Dispatchers.IO).launch {
                listCourtsWithReservations = db.courtDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
                listCourtsWithReservations = if(filterVM.getSportFilter() != null) listCourtsWithReservations.filter { it.court.sport == filterVM.getSportFilter() } else listCourtsWithReservations.filter { userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() }  }
                Log.e("list", listCourtsWithReservations.toString())
                vm.listAvailableReservations.postValue(listCourtsWithReservations)
            }
        }

    }

    // TODO
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

class AdapterNewGames(private var listAvailableReservations: List<CourtWithReservations>, var listener: OnClickTimeslot): RecyclerView.Adapter<ViewHolderNewGames>(){

    interface OnClickTimeslot {
        fun onClickTimeslot(informations: Bundle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNewGames {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.available_booking_card, parent, false)
        return ViewHolderNewGames(v)
    }

    override fun getItemCount(): Int {
        return listAvailableReservations.size
    }

    override fun onBindViewHolder(holder: ViewHolderNewGames, position: Int) {
            val court = listAvailableReservations[position].court
            val timeSlots = listAvailableReservations[position].reservations.map { it.time }

            holder.timeslots.removeAllViews()

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
                tv.setOnClickListener {
                    Log.d("db", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].toString())
                    Log.d("db", listAvailableReservations[position].court.toString())
                    val currentGameBundle = Bundle().apply {
                        putInt("reservationId", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].reservationId)
                        putString("date", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].date.toString())
                        putString("time", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].time.toString())
                        putDouble("price", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].price)
                        putInt("numOfPlayers", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].numOfPlayers)
                        putInt("courtId", listAvailableReservations[position].reservations[timeSlots.indexOf(t)].courtId)
                        putString("courtName", listAvailableReservations[position].court.name)
                        putString("sport", listAvailableReservations[position].court.sport)
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

    fun setListCourts(newListCourts: List<CourtWithReservations>) {

        val diffs = DiffUtil.calculateDiff(
            CourtTimeslotDiffCallback(listAvailableReservations, newListCourts)
        )
        listAvailableReservations = newListCourts
        diffs.dispatchUpdatesTo(this)
    }
}

class ViewHolderFilter(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.filter_name)
    val layout: LinearLayout = v.findViewById(R.id.filter_button_layout)
}

class AdapterFilters(private var listOfSport: List<String?>, val setFilter: (input: String?) -> Unit): RecyclerView.Adapter<ViewHolderFilter>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFilter {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_button, parent, false)
        return ViewHolderFilter(v)
    }

    override fun getItemCount(): Int = listOfSport.size

    override fun onBindViewHolder(holder: ViewHolderFilter, position: Int) {
        val name = listOfSport[position]

        holder.name.text = name?:"All"
        holder.layout.setOnClickListener {
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
