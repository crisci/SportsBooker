package com.example.lab2.reservation.my_reservations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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
import com.example.lab2.R
import com.example.lab2.common.calendar.setTextColorRes
import com.example.lab2.reservation.book_reservation.BookReservationActivity
import com.example.lab2.reservation.details.DetailsActivity
import com.example.lab2.reservation.edit_reservation.EditReservationActivity
import com.example.lab2.view_models.CalendarVM
import com.example.lab2.view_models.MainVM
import com.example.lab2.view_models.MyReservationsVM
import com.example.lab2.view_models.RatingModalVM
import com.example.lab2.entities.firebase.MatchWithCourtAndEquipments
import com.example.lab2.view_models.ReservationVM
import com.example.lab2.entities.firebase.formatPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MyReservationsFragment : Fragment(R.layout.fragment_my_reservations),
    AdapterCard.OnEditClickListener {

    // TODO : this is hard-coded for now
    val playerId = 1

    private lateinit var navController: NavController

    @Inject
    lateinit var userVM: MainVM
    lateinit var vm: MyReservationsVM
    lateinit var calendarVM: CalendarVM
    lateinit var ratingModalVM: RatingModalVM

    lateinit var resVM: ReservationVM

    private lateinit var adapterCardFilters: AdapterFilterReservation

    private lateinit var noResults: ConstraintLayout
    private lateinit var findNewGamesButton: Button
    private lateinit var leaveRatingLayout: ConstraintLayout
    var showBanner = false


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if (response.resultCode == AppCompatActivity.RESULT_OK) {
            vm.setSportFilter(null)
        }
    }

    private fun showOrHideNoResultImage() {
        if (vm.getMyReservations().value.isNullOrEmpty()) {
            noResults.visibility = View.VISIBLE
        } else {
            noResults.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(requireActivity())[MyReservationsVM::class.java]
        calendarVM = ViewModelProvider(requireActivity())[CalendarVM::class.java]
        ratingModalVM = ViewModelProvider(requireActivity())[RatingModalVM::class.java]
        resVM = ViewModelProvider(requireActivity())[ReservationVM::class.java]

        vm.refreshMyReservations(
            userVM.userId,
            calendarVM.getSelectedDate().value!!,
            calendarVM.getSelectedTime().value!!,
            userVM.user.value!!.interests
        )

        leaveRatingLayout = view.findViewById(R.id.leave_rating_banner)

        /*        CoroutineScope(Dispatchers.IO).launch {
                    // Check if the rating dialog should be shown
                    // doing a GET every minute
                    while (true) {
                        ratingModalVM.checkIfPlayerHasAlreadyReviewed(playerId)
                        delay(60000) // 60 seconds
                    }
                }*/

        /*        ratingModalVM.getShowBanner().observe(viewLifecycleOwner) {
                    if (it == true) leaveRatingLayout.visibility = View.VISIBLE
                    else leaveRatingLayout.visibility = View.GONE
                }

                ratingModalVM.getCourtToReview().observe(viewLifecycleOwner) {
                    leaveRatingLayout.visibility = View.VISIBLE
                    leaveRatingLayout.setOnClickListener {
                        val modalBottomSheet = RatingModalBottomSheet()
                        modalBottomSheet.show(childFragmentManager, RatingModalBottomSheet.TAG)
                    }
                }*/

        navController = findNavController()
        requireActivity().actionBar?.elevation = 0f

        val adapterCard = AdapterCard(emptyList(), this)
        val listReservationsRecyclerView =
            view.findViewById<RecyclerView>(R.id.your_reservation_recycler_view)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        //TODO: Initialize with user interests
        adapterCardFilters = AdapterFilterReservation(
            listOf(null)
                .plus(userVM.user.value!!.interests
                    .map { sport ->
                        sport.name.lowercase().replaceFirstChar { it.uppercase() }
                    }),
            vm::setSportFilter
        )
        val listOfSportRecyclerView = view.findViewById<RecyclerView>(R.id.my_reservation_filter)
        listOfSportRecyclerView.adapter = adapterCardFilters
        listOfSportRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        noResults = view.findViewById(R.id.no_results)

        userVM.user.observe(viewLifecycleOwner) {
            adapterCardFilters.setFilters(
                listOf(null)
                    .plus(userVM.user.value!!.interests
                        .map { sport ->
                            sport.name.lowercase().replaceFirstChar { it.uppercase() }
                        })
            )
        }

        vm.getMyReservations().observe(viewLifecycleOwner) {
            adapterCard.setReservations(it)
            showOrHideNoResultImage()
            Log.e("test", vm.getMyReservations().value.toString())
        }

        calendarVM.getSelectedDate().observe(requireActivity()) {
            vm.refreshMyReservations(
                userVM.userId,
                calendarVM.getSelectedDate().value!!,
                calendarVM.getSelectedTime().value!!,
                userVM.user.value!!.interests.toList()
            )
        }

        vm.getSportFilter().observe(viewLifecycleOwner) {
            if (it == null) {
                adapterCardFilters.selectedPosition = 0
                adapterCardFilters.notifyDataSetChanged()
            }
            vm.refreshMyReservations(
                userVM.userId,
                calendarVM.getSelectedDate().value!!,
                calendarVM.getSelectedTime().value!!,
                userVM.user.value!!.interests.toList()
            )
        }

        calendarVM.getSelectedTime().observe(viewLifecycleOwner) {
            vm.refreshMyReservations(
                userVM.userId,
                calendarVM.getSelectedDate().value!!,
                calendarVM.getSelectedTime().value!!,
                userVM.user.value!!.interests.toList()
            )
        }


        findNewGamesButton = view.findViewById(R.id.find_new_games_button)
        findNewGamesButton.setOnClickListener {
            resVM.getPlayerReservations(userVM.userId)
            val intentBookReservation =
                Intent(requireContext(), BookReservationActivity::class.java)
            launcher.launch(intentBookReservation)
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            vm.refreshMyReservations(
                userVM.userId,
                calendarVM.getSelectedDate().value!!,
                calendarVM.getSelectedTime().value!!,
                userVM.user.value!!.interests.toList()
            )
            swipeRefreshLayout.isRefreshing = false
        }

        //TODO this tutorial must show after the user has registered theirselves, so at first login
        /* calendarVM.getShowTutorial().observe(viewLifecycleOwner) {
             val builder = AlertDialog.Builder(requireContext())
             val view = layoutInflater.inflate(R.layout.modal_tutorial, null)
             builder.setView(view)
             builder.setTitle("Swipe horizontally to change the week")
             builder.setPositiveButton("OK") { dialog, which ->
             }
             val dialog = builder.create()
             dialog.show()
         }*/

    }

    override fun onResume() {
        super.onResume()
        //When the activity become again visible the filter is setted to null
        //So that if onBackPressed the filter is resetted
        vm.setSportFilter(null)
    }


    override fun onEditClick(reservation: MatchWithCourtAndEquipments) {

        val jsonString = Json.encodeToString(MatchWithCourtAndEquipments.serializer(), reservation)

        val intentEditReservation = Intent(activity, EditReservationActivity::class.java).apply {
            addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
            putExtra("reservationJson", jsonString)
        }
        launcher.launch(intentEditReservation)
    }

}

class ViewHolderCard(v: View) : RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val currentNumberOfPlayers: TextView = v.findViewById(R.id.current_number_of_players)
    val price: TextView = v.findViewById(R.id.price_reservation)
    val maxNumberOfPlayers: TextView = v.findViewById(R.id.max_number_players)
    val time: TextView = v.findViewById(R.id.time_reservation)
    val editButton: ImageButton = v.findViewById(R.id.edit_reservation_button)
    val sport: TextView = v.findViewById(R.id.sport_name)
    val detailsButton: Button = v.findViewById(R.id.detailReservationButton)
    val context: Context = v.context
    val maxNumber: TextView = v.findViewById(R.id.max_number_players)
}

class AdapterCard(
    private var list: List<MatchWithCourtAndEquipments>,
    private val listener: OnEditClickListener
) : RecyclerView.Adapter<ViewHolderCard>() {

    interface OnEditClickListener {
        fun onEditClick(reservation: MatchWithCourtAndEquipments)
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
        holder.name.text = "${list[position].court.name}"
        holder.location.text = "Via Giovanni Magni, 32"
        holder.price.text = "${list[position].formatPrice()}"
        if (list[position].match.numOfPlayers == list[position].court.maxNumberOfPlayers) {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.darker_blue)
        } else {
            holder.currentNumberOfPlayers.setTextColorRes(R.color.example_1_bg)
        }
        holder.currentNumberOfPlayers.text = "${list[position].match.numOfPlayers}"
        if (list[position].match.numOfPlayers == list[position].court.maxNumberOfPlayers) holder.currentNumberOfPlayers.setTextColor(
            holder.context.getColor(
                R.color.bright_red
            )
        )
        holder.maxNumberOfPlayers.text = "/${list[position].court.maxNumberOfPlayers}"
        holder.time.text =
            list[position].match.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
        holder.sport.text = "${list[position].court.sport}"

        holder.editButton.setOnClickListener { listener.onEditClick(list[holder.bindingAdapterPosition]) }
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.context, DetailsActivity::class.java)
            intent.putExtra("reservationId", list[position].reservationId)
            holder.context.startActivity(intent)
        }
    }

    fun setReservations(newReservations: List<MatchWithCourtAndEquipments>) {

        val diffs = DiffUtil.calculateDiff(
            ReservationDiffCallback(list, newReservations)
        )
        list = newReservations
        diffs.dispatchUpdatesTo(this)
    }

}

class ViewHolderFilterReservation(v: View) : RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.filter_name)
    val layout: ConstraintLayout = v.findViewById(R.id.filter_button_layout)
    val selectionIndicator: View = v.findViewById(R.id.selectionIndicator)
}

class AdapterFilterReservation(
    private var listOfSport: List<String?>,
    val setFilter: (input: String?) -> Unit
) : RecyclerView.Adapter<ViewHolderFilterReservation>() {


    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFilterReservation {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_button, parent, false)
        return ViewHolderFilterReservation(v)
    }

    override fun getItemCount(): Int = listOfSport.size

    override fun onBindViewHolder(holder: ViewHolderFilterReservation, position: Int) {
        val name = listOfSport[position]

        holder.name.text = name ?: "All"
        holder.selectionIndicator.visibility =
            if (selectedPosition == holder.bindingAdapterPosition) View.VISIBLE else View.GONE

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