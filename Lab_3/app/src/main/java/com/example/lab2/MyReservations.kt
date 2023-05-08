package com.example.lab2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.database.reservation.formatPrice
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MyReservations : Fragment(R.layout.fragment_my_reservations), AdapterCard.OnEditClickListener  {

    // TODO : this is hard-coded for now
    val playerId = 1

    private lateinit var navController : NavController
    @Inject
    lateinit var vm: CalendarViewModel

    @Inject
    lateinit var filterVM: FilterViewModel

    @Inject
    lateinit var userVM: UserViewModel

    private var list = listOf<ReservationWithCourtAndEquipments>()

    lateinit private var adapterCardFilters: AdapterFilterReservation

    private lateinit var db: ReservationAppDatabase
    private lateinit var filteredList: List<ReservationWithCourtAndEquipments>

    private lateinit var noResults: ConstraintLayout
    private lateinit var findNewGamesButton: Button

    private suspend fun getMyReservations() {
        list = db.playerDao().loadReservationsByPlayerId(1, vm.selectedDate.value!!)
        filteredList = if(filterVM.getSportFilter() != null ) list.filter { it.reservation.date.dayOfYear == vm.selectedDate.value?.dayOfYear && it.court.sport == filterVM.getSportFilter() && userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() } } else list.filter { it.reservation.date.dayOfYear == vm.selectedDate.value?.dayOfYear && userVM.getUser().interests.any { sport -> sport.name == it.court.sport.uppercase() }  }
        showOrHideNoResultImage()
        vm.list.postValue(filteredList)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            filterVM.setSportFilter(null)
            vm.selectedDate.value = LocalDate.now()
        }
    }

    private suspend fun showOrHideNoResultImage() {
        withContext(Dispatchers.Main) {
            if (list.isNotEmpty()) {
                noResults.visibility = View.GONE
            } else {
                noResults.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = ReservationAppDatabase.getDatabase(requireContext())
        filteredList = emptyList()
        navController = findNavController()
        requireActivity().actionBar?.elevation = 0f

        val adapterCard = AdapterCard(filteredList, this )
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.your_reservation_recycler_view)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapterCardFilters = AdapterFilterReservation(
            listOf(null)
                .plus(userVM.getUser().interests
                .map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }),
            filterVM::setSportFilter
        )
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.my_reservation_filter)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        noResults = view.findViewById(R.id.no_results)

        userVM.user.observe(viewLifecycleOwner) {
            adapterCardFilters.setFilters(
                listOf(null)
                    .plus(userVM.getUser().interests
                        .map { sport -> sport.name.lowercase().replaceFirstChar { it.uppercase() } }))
        }

        vm.list.observe(requireActivity()){
            adapterCard.setReservations(vm.list.value!!)
        }

        vm.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                filterVM.sportFilter.postValue(null)
                getMyReservations()
            }
        }

        filterVM.sportFilter.observe(viewLifecycleOwner) {
            if(it == null) {
                adapterCardFilters.selectedPosition = 0
                adapterCardFilters.notifyDataSetChanged()
            }
            CoroutineScope(Dispatchers.IO).launch {
                getMyReservations()
            }
        }


        findNewGamesButton = view.findViewById(R.id.find_new_games_button)
        findNewGamesButton.setOnClickListener {
            val intentBookReservation = Intent(requireContext(), BookReservationActivity::class.java)
            launcher.launch(intentBookReservation)
        }
    }

    override fun onResume() {
        super.onResume()
        //When the activity become again visible the filter is setted to null
        //So that if onBackPressed the filter is resetted
        filterVM.setSportFilter(null)
        vm.selectedDate.value = LocalDate.now()
    }



    override fun onEditClick(reservation: ReservationWithCourtAndEquipments) {

        val intentEditReservation = Intent(activity, EditReservationActivity::class.java).apply {
            addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
            putExtra("reservationId", reservation.reservation.reservationId)
            putExtra("date", reservation.reservation.date.toString())
            putExtra("time", reservation.reservation.time.toString())
            putExtra("price", reservation.reservation.price)
            putExtra("numOfPlayers", reservation.reservation.numOfPlayers)
            putExtra("courtId", reservation.court.courtId)
            putExtra("courtName", reservation.court.name)
            putExtra("sport", reservation.court.sport)
            putExtra("equipments", Gson().toJson(reservation.equipments))
            putExtra("finalPrice", reservation.finalPrice)
            putExtra("playerId", playerId)
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
    val sport : TextView = v.findViewById(R.id.sport_name)
}

class AdapterCard(private var list: List<ReservationWithCourtAndEquipments>, private val listener: OnEditClickListener): RecyclerView.Adapter<ViewHolderCard>() {

    interface OnEditClickListener {
        fun onEditClick(reservation: ReservationWithCourtAndEquipments)
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
        holder.name.text = "${list[position].court.name}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.price.text = "€ ${list[position].formatPrice()}"
        if(list[position].reservation.numOfPlayers == maxNumPlayers) {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.example_1_bg)
        }
        else {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.bright_red)
        }
        holder.currentNumberOfPlayers.text = "${list[position].reservation.numOfPlayers}"
        holder.maxNumberOfPlayers.text = "/7"
        holder.time.text = list[position].reservation.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
        holder.sport.text = "${list[position].court.sport}"

        holder.editButton.setOnClickListener { listener.onEditClick(list[holder.bindingAdapterPosition]) }
    }

    fun setReservations(newReservations: List<ReservationWithCourtAndEquipments>) {

        val diffs = DiffUtil.calculateDiff(
            ReservationDiffCallback(list, newReservations)
        )
        list = newReservations
        diffs.dispatchUpdatesTo(this)
    }

}

class ViewHolderFilterReservation(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.filter_name)
    val layout: ConstraintLayout = v.findViewById(R.id.filter_button_layout)
    val selectionIndicator: View = v.findViewById(R.id.selectionIndicator)
}

class AdapterFilterReservation(private var listOfSport: List<String?>, val setFilter: (input: String?) -> Unit): RecyclerView.Adapter<ViewHolderFilterReservation>(){


    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFilterReservation {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_button, parent, false)
        return ViewHolderFilterReservation(v)
    }

    override fun getItemCount(): Int = listOfSport.size

    override fun onBindViewHolder(holder: ViewHolderFilterReservation, position: Int) {
        val name = listOfSport[position]

        holder.name.text = name?:"All"
        holder.selectionIndicator.visibility = if (selectedPosition == holder.bindingAdapterPosition) View.VISIBLE else View.GONE

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