package com.example.lab2

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
import com.example.lab2.calendar.setTextColorRes
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.CourtWithReservations
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

    private var listCourtsWithReservations = listOf<CourtWithReservations>()

    private lateinit var db: ReservationAppDatabase


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = response.data
            CoroutineScope(Dispatchers.IO).launch {
               listCourtsWithReservations = db.courtDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
               vm.listAvailableReservations.postValue(listCourtsWithReservations)
            }
            navController.navigate(R.id.action_newGames_to_myReservations)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = ReservationAppDatabase.getDatabase(requireContext())
        navController = findNavController()
        val adapterCard = AdapterNewGames(listCourtsWithReservations, this)
        val listReservationsRecyclerView = view.findViewById<RecyclerView>(R.id.available_bookings)
        listReservationsRecyclerView.adapter = adapterCard
        listReservationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        vm.listAvailableReservations.observe(requireActivity()){
            adapterCard.setListCourts(vm.listAvailableReservations.value!!)
        }

        vm.selectedDate.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                listCourtsWithReservations = db.courtDao().getAvailableReservationsByDate(vm.selectedDate.value!!)
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
                // TODO When the user presses a timeslot, a window ConfirmBooking will open
                tv.setOnClickListener {
                    //TODO: save the content of the selected game
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