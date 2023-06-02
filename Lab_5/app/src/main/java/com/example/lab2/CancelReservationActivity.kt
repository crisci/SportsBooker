package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.viewmodels.EditReservationViewModel
import com.example.lab2.viewmodels.MyReservationsVM
import com.example.lab2.viewmodels.SignupVM
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class CancelReservationActivity : AppCompatActivity() {

    private lateinit var editReservationVM: EditReservationViewModel

    private lateinit var reservation: MatchWithCourtAndEquipments

    private lateinit var sport_name: TextView
    private lateinit var court_name_cancel_reservation: TextView
    private lateinit var location_cancel_reservation: TextView
    private lateinit var date_cancel_reservation: TextView
    private lateinit var time_cancel_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var menuItem: MenuItem
    private lateinit var backButton: ImageView

    @Inject
    lateinit var mainVM: MainVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Cancel)
        setContentView(R.layout.activity_cancel_reservation)
        setSupportActionBar()
        findViews()

        editReservationVM = ViewModelProvider(this)[EditReservationViewModel::class.java]

        val reservationString = intent.getStringExtra("resString")
        reservation = Json.decodeFromString(MatchWithCourtAndEquipments.serializer(), reservationString!!)

        updateContent()

        cancelButton.setOnClickListener{
            try{
                val result: Intent = Intent()
                result.putExtra("result", true)
                editReservationVM.cancelReservation(mainVM.userId, reservation)
                setResult(Activity.RESULT_OK, result)
                finish()
            }catch (err: Exception){
                Toast.makeText(applicationContext, "${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findViews(){
        sport_name = findViewById(R.id.sport_name_cancel_reservation)
        court_name_cancel_reservation = findViewById(R.id.court_name_cancel_reservation)
        location_cancel_reservation = findViewById(R.id.location_cancel_reservation)
        date_cancel_reservation = findViewById(R.id.date_cancel_reservation)
        time_cancel_reservation = findViewById(R.id.time_cancel_reservation)
        cancelButton = findViewById(R.id.cancel_button_cancel_reservation)
    }

    private fun setSupportActionBar(){
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this,R.color.bright_red))
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Cancel my reservation"

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }


    private fun updateContent() {
        sport_name.text = "${reservation.court.sport}"
        court_name_cancel_reservation.text = "${reservation.court.name}"
        location_cancel_reservation.text = "Via Giovanni Magni, 32"
        val dayMonth = reservation.match.date.format(DateTimeFormatter.ofPattern("dd MMM")).split(" ")
        val day = dayMonth[0]
        val month = dayMonth[1].replaceFirstChar { it.uppercase() }
        val formattedDate = "$day $month"
        date_cancel_reservation.text = formattedDate
        time_cancel_reservation.text = reservation.match.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }
}