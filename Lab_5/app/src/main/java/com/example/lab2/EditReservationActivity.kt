package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.lab2.viewmodels.EditReservationViewModel
import com.example.lab2.viewmodels.EquipmentsVM
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationTimeslot
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Equipment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class EditReservationActivity : AppCompatActivity() {

    private lateinit var res : Reservation
    private lateinit var oldReservation: ReservationWithCourtAndEquipments
    private lateinit var court_name_edit_reservation: TextView
    private lateinit var location_edit_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var priceText: TextView
    private lateinit var sport_name: TextView

    private lateinit var chipGroup: ChipGroup
    var selectedText: String = ""
    var time: String? = ""
    private var noTimeslotSelected = false

    private lateinit var listReservationTimeslot: MutableList<ReservationTimeslot>

    lateinit var equipmentsVM: EquipmentsVM
    lateinit var editReservationVM: EditReservationViewModel

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

        equipmentsVM = ViewModelProvider(this)[EquipmentsVM::class.java]
        editReservationVM = ViewModelProvider(this)[EditReservationViewModel::class.java]

        //db = ReservationAppDatabase.getDatabase(this)
        sport_name = findViewById(R.id.sport_name_edit_reservation)
        court_name_edit_reservation = findViewById(R.id.court_name_edit_reservation)
        location_edit_reservation = findViewById(R.id.location_edit_reservation)
        priceText = findViewById(R.id.local_price_edit_reservation)
        saveButton = findViewById(R.id.save_button_edit_reservation)
        chipGroup = findViewById(R.id.time_slots_chip_group_edit_reservation)
        cancelButton = findViewById(R.id.cancel_button_edit_reservation)
        cancelButton.setOnClickListener {
            val intent = Intent(this, CancelReservationActivity::class.java).apply {
                putExtra("reservationId", editReservationVM.getCurrentReservation().value!!.reservation.reservationId)
                putExtra("date", editReservationVM.getCurrentReservation().value!!.reservation.date.toString())
                putExtra("time", editReservationVM.getCurrentReservation().value!!.reservation.time.toString())
                putExtra("price", editReservationVM.getCurrentReservation().value!!.reservation.price)
                putExtra("numOfPlayers", editReservationVM.getCurrentReservation().value!!.reservation.numOfPlayers)
                putExtra("courtId", editReservationVM.getCurrentReservation().value!!.court.courtId)
                putExtra("courtName", editReservationVM.getCurrentReservation().value!!.court.name)
                putExtra("sport", editReservationVM.getCurrentReservation().value!!.court.sport)
            }
            launcher.launch(intent)
        }

        val playerId = intent.getIntExtra("playerId", 0)
        val reservationId = intent.getIntExtra("reservationId", 0)
        val date = intent.getStringExtra("date")
        time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)
        val courtId = intent.getIntExtra("courtId", 0)
        val courtName = intent.getStringExtra("courtName")
        val sport = intent.getStringExtra("sport")
        val equipmentsString = intent.getStringExtra("equipments")
        val finalPrice = intent.getDoubleExtra("finalPrice", 0.0)

        val listType = object : TypeToken<MutableList<Equipment>>() {}.type
        val equipments: MutableList<Equipment> = Gson().fromJson(equipmentsString, listType)
        equipmentsVM.setEquipments(equipments)

        sport_name.text = sport
        court_name_edit_reservation.text = courtName
        location_edit_reservation.text = "Via Giovanni Magni, 32"

        // Fetch from the db the other reservations for the same court
        editReservationVM.fetchAvailableReservations(
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
            1,
            reservationId,
            sport!!,
            LocalTime.parse(time)
        )

        editReservationVM.setCurrentReservation(
            ReservationWithCourtAndEquipments(
                Reservation(reservationId,courtId,numOfPlayers,price,LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time)),
                Court(courtId, courtName!!, sport!!, 0),
                equipments,
                finalPrice
            )
        )

        editReservationVM.getAvailableTimeslot().observe(this) {
            updateContent()
        }

        supportActionBar?.elevation = 0f
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
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
            submitChanges()
        }

    }

    private fun submitChanges() {
        try{
            val newReservationId = editReservationVM.getAvailableTimeslot().value!!
                .find {
                    it.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()  == selectedText
                }?.reservationId ?: editReservationVM.getCurrentReservation().value!!.reservation.reservationId

            editReservationVM.updateReservation(
                playerId = 1,
                newReservationId = newReservationId,
                newFinalPrice = equipmentsVM.getPersonalPrice().value!!,
                newEquipments = equipmentsVM.getEquipments().value!!,
            )
            setResult(Activity.RESULT_OK)
            finish()
        }catch(err: Exception) {
            when(err) {
                is SQLiteConstraintException ->
                    Toast.makeText(applicationContext, "This timeslot is already booked.", Toast.LENGTH_SHORT).show()
                else ->
                    Toast.makeText(applicationContext, "Unable to update your reservation.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateContent() {

        setupCheckboxes(editReservationVM.getCurrentReservation().value!!.finalPrice)
        for (r in editReservationVM.getAvailableTimeslot().value!!) {
            val inflater = LayoutInflater.from(chipGroup.context)
            val chip = inflater.inflate(R.layout.timeslot_chip, chipGroup, false) as Chip
            chip.isClickable = true
            chip.isCheckable = true
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

        checkboxContainer.removeAllViews()

        equipmentsVM.setPersonalPrice(startingPrice)

        val listEquipments = equipmentsVM.getListEquipments(editReservationVM.getCurrentReservation().value!!.court.sport)

        for (e in listEquipments) {
            val checkbox = CheckBox(this)
            checkbox.text = "${e.name} - €${e.price}"

            if(editReservationVM.getCurrentReservation().value!!.equipments.any { it.name == e.name })
                checkbox.isChecked = true

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    equipmentsVM.setPersonalPrice(equipmentsVM.getPersonalPrice().value?.plus(e.price)!!)
                    equipmentsVM.addEquipment(e)
                }
                else {
                    equipmentsVM.setPersonalPrice(equipmentsVM.getPersonalPrice().value?.minus(e.price)!!)
                    equipmentsVM.removeEquipment(e)
                }
            }
            checkboxContainer.addView(checkbox)
        }

        equipmentsVM.getPersonalPrice().observe(this) {
            priceText.text = "You will pay €${String.format("%.02f", it)} locally."
        }
    }

}