package com.example.lab2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.entities.User
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CancelReservationActivity : AppCompatActivity() {

    private lateinit var reservation: Reservation
    private lateinit var court_name_cancel_reservation: TextView
    private lateinit var location_cancel_reservation: TextView
    private lateinit var date_cancel_reservation: TextView
    private lateinit var time_cancel_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var menuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_reservation)

        court_name_cancel_reservation = findViewById(R.id.court_name_cancel_reservation)
        location_cancel_reservation = findViewById(R.id.location_cancel_reservation)
        date_cancel_reservation = findViewById(R.id.date_cancel_reservation)
        time_cancel_reservation = findViewById(R.id.time_cancel_reservation)
        cancelButton = findViewById(R.id.cancel_button_cancel_reservation)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar_cancel)
        supportActionBar?.elevation = 0f

        val reservationId = intent.getIntExtra("reservationId", 0)
        val courtId = intent.getIntExtra("courtId", 0)
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)

        reservation = Reservation(reservationId,courtId,numOfPlayers,price, LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.now())
        updateContent()

        cancelButton.setOnClickListener{
            val result: Intent = Intent()
            result.putExtra("result", true)
            setResult(Activity.RESULT_OK, result)
            finish()
        }

    }

    private fun updateContent() {
        court_name_cancel_reservation.text = "Campo ${reservation.courtId}"
        location_cancel_reservation.text = "Corso Duca degli Abruzzi 24, Torino"
        time_cancel_reservation.text = reservation.date.toString()
        date_cancel_reservation.text = reservation.time.toString()
    }
}