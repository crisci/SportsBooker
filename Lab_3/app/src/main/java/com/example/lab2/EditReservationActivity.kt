package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lab2.calendar.BookingViewModel
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.calendar.UserViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtWithReservations
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationTimeslot
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.database.reservation.formatPrice
import com.example.lab2.entities.Equipment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class EditReservationActivity : AppCompatActivity() {

    private lateinit var res : Reservation
    private lateinit var reservation:ReservationWithCourtAndEquipments
    private lateinit var court_name_edit_reservation: TextView
    private lateinit var location_edit_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var db: ReservationAppDatabase
    private lateinit var backButton: ImageView
    private lateinit var priceText: TextView
    private lateinit var sport_name: TextView

    private lateinit var chipGroup: ChipGroup
    var selectedText: String = ""
    var time: String? = ""
    private var noTimeslotSelected = false

    private lateinit var equipments: MutableList<Equipment>
    private lateinit var listReservationTimeslot: MutableList<ReservationTimeslot>

    @Inject
    lateinit var vm: CalendarViewModel

    @Inject
    lateinit var userVM: UserViewModel

    @Inject
    lateinit var bookingViewModel: BookingViewModel

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            val data: Intent? = response.data
            val cancel = data?.getBooleanExtra("result", false)
            if(cancel == true) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reservation)

        db = ReservationAppDatabase.getDatabase(this)
        sport_name = findViewById(R.id.sport_name_edit_reservation)
        court_name_edit_reservation = findViewById(R.id.court_name_edit_reservation)
        location_edit_reservation = findViewById(R.id.location_edit_reservation)
        priceText = findViewById(R.id.local_price_edit_reservation)
        saveButton = findViewById(R.id.save_button_edit_reservation)
        chipGroup = findViewById(R.id.time_slots_chip_group_edit_reservation)
        cancelButton = findViewById(R.id.cancel_button_edit_reservation)
        cancelButton.setOnClickListener {
            val intent = Intent(this, CancelReservationActivity::class.java).apply {
                putExtra("reservationId", reservation.reservation.reservationId)
                putExtra("date", reservation.reservation.date.toString())
                putExtra("time", reservation.reservation.time.toString())
                putExtra("price", reservation.reservation.price)
                putExtra("numOfPlayers", reservation.reservation.numOfPlayers)
                putExtra("courtId", reservation.court.courtId)
                putExtra("courtName", reservation.court.name)
                putExtra("sport", reservation.court.sport)
            }
            launcher.launch(intent)
        }

        val playerId = intent.getIntExtra("playerId", 0)
        val reservationId = intent.getIntExtra("reservationId", 0)
        val date = intent.getStringExtra("date")
        time = intent.getStringExtra("time")
        listReservationTimeslot = mutableListOf(ReservationTimeslot(reservationId,LocalTime.parse(time)))
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)
        val courtId = intent.getIntExtra("courtId", 0)
        val courtName = intent.getStringExtra("courtName")
        val sport = intent.getStringExtra("sport")
        val equipmentsString = intent.getStringExtra("equipments")
        val finalPrice = intent.getDoubleExtra("finalPrice", 0.0)

        val listType = object : TypeToken<MutableList<Equipment>>() {}.type
        equipments = Gson().fromJson(equipmentsString, listType)

        thread {
            val list = db.playerReservationDAO().
            getPlayerAvailableReservationsByDate(LocalDate.parse(date, DateTimeFormatter.ISO_DATE), playerId, sport!!)
            listReservationTimeslot = listReservationTimeslot.plus(list).sortedBy { it.time } as MutableList<ReservationTimeslot>
        }


        res = Reservation(reservationId,courtId,numOfPlayers,price,LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time))
        reservation = ReservationWithCourtAndEquipments(res, Court(courtId, courtName!!, sport!!, 0),equipments,finalPrice)
        updateContent()

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Edit Reservations"

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        saveButton.setOnClickListener {

            if (noTimeslotSelected) {
                Toast.makeText(applicationContext, "Please, select a timeslot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try{

                    val newReservationId = listReservationTimeslot.find { it.time.format(
                        DateTimeFormatter.ofPattern("HH:mm")).toString()  == selectedText }?.reservationId ?: reservationId

                    db.playerReservationDAO().updateReservation(
                        playerId = playerId,
                        reservationId = reservationId,
                        newReservationId = newReservationId,
                        newEquipments = equipments,
                        newFinalPrice = bookingViewModel.personalPrice.value!!)

                    db.reservationDao().updateNumOfPlayers(reservationId)
                    db.reservationDao().updateNumOfPlayers(newReservationId)
                    userVM.listBookedReservations.postValue(userVM.listBookedReservations.value?.minus(reservationId)?.plus(newReservationId) as MutableSet<Int>)
                    setResult(Activity.RESULT_OK)
                    finish()
                }catch(err: Exception) {
                    when(err) {
                        is SQLiteConstraintException -> withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "This timeslot is already booked.", Toast.LENGTH_SHORT).show()
                        }
                        else -> withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Unable to update your reservation.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


        }

    }

    private fun updateContent() {
        sport_name.text = "${reservation.court.sport}"
        court_name_edit_reservation.text = "${reservation.court.name}"
        location_edit_reservation.text = "Via Giovanni Magni, 32"

        setupCheckboxes(reservation.finalPrice)

        for (r in listReservationTimeslot) {
            val chip = Chip(this)
            chip.isClickable = true
            chip.isCheckable = true
            chip.setChipBackgroundColorResource(androidx.appcompat.R.color.material_blue_grey_800)
            chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            chip.setChipStrokeColorResource(R.color.white)
            chip.chipStrokeWidth = 2F
            chip.text = "${r.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            if (chip.text == time) chip.isChecked = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chip.setChipBackgroundColorResource(R.color.darker_blue)
                } else {
                    chip.setChipBackgroundColorResource(androidx.appcompat.R.color.material_blue_grey_800)
                }
            }
            chipGroup.addView(chip)
        }
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if(checkedIds.isNotEmpty()) {
                val selectedChip = findViewById<Chip>(checkedIds.first())
                selectedText = selectedChip.text.toString()
                noTimeslotSelected = false
            }
            else {
                noTimeslotSelected = true
            }
        }
    }

    private fun setupCheckboxes(startingPrice: Double) {

        val checkboxContainer = findViewById<LinearLayout>(R.id.checkbox_container)

        bookingViewModel.setPersonalPrice(startingPrice)

        val listEquipments = bookingViewModel.getListEquipments(reservation.court.sport)

        for (e in listEquipments) {
            val checkbox = CheckBox(this)
            checkbox.text = "${e.name} - €${e.price}"

            if(reservation.equipments.any { it.name == e.name })
                checkbox.isChecked = true

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    bookingViewModel.setPersonalPrice(bookingViewModel.personalPrice.value?.plus(e.price)!!)
                    equipments.add(e)
                }
                else {
                    bookingViewModel.setPersonalPrice(bookingViewModel.personalPrice.value?.minus(e.price)!!)
                    equipments.remove(e)
                }
            }
            checkboxContainer.addView(checkbox)
        }

        bookingViewModel.personalPrice.observe(this) {
            priceText.text = "You will pay €${String.format("%.02f", bookingViewModel.personalPrice.value)} locally."
        }
    }

}