package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lab2.database.reservation.Reservation
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class CancelReservationActivity : AppCompatActivity() {

    private lateinit var reservation: Reservation
    private lateinit var court_name_cancel_reservation: TextView
    private lateinit var location_cancel_reservation: TextView
    private lateinit var date_cancel_reservation: TextView
    private lateinit var time_cancel_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var menuItem: MenuItem
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Cancel)
        setContentView(R.layout.activity_cancel_reservation)

        court_name_cancel_reservation = findViewById(R.id.court_name_cancel_reservation)
        location_cancel_reservation = findViewById(R.id.location_cancel_reservation)
        date_cancel_reservation = findViewById(R.id.date_cancel_reservation)
        time_cancel_reservation = findViewById(R.id.time_cancel_reservation)
        cancelButton = findViewById(R.id.cancel_button_cancel_reservation)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this,R.color.bright_red))
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Cancel my reservation"


        val reservationId = intent.getIntExtra("reservationId", 0)
        val courtId = intent.getIntExtra("courtId", 0)
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)

        reservation = Reservation(reservationId,courtId,numOfPlayers,price, LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time))
        updateContent()

        cancelButton.setOnClickListener{
            val result: Intent = Intent()
            result.putExtra("result", true)
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun updateContent() {
        court_name_cancel_reservation.text = "Campo ${reservation.courtId}"
        location_cancel_reservation.text = "Corso Duca degli Abruzzi 24, Torino"
        val dayMonth = reservation.date.format(DateTimeFormatter.ofPattern("dd MMM")).split(" ")
        val day = dayMonth[0]
        val month = dayMonth[1].replaceFirstChar { it.uppercase() }
        val formattedDate = "$day $month"
        date_cancel_reservation.text = formattedDate
        time_cancel_reservation.text = reservation.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }
}