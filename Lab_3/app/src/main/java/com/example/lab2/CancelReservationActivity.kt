package com.example.lab2

import android.app.Activity
import android.content.Intent
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
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class CancelReservationActivity : AppCompatActivity() {

    private lateinit var db: ReservationAppDatabase
    private lateinit var reservation: Reservation
    private lateinit var court: Court
    private lateinit var sport_name: TextView
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

        db = ReservationAppDatabase.getDatabase(this)
        sport_name = findViewById(R.id.sport_name_cancel_reservation)
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
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)
        val courtId = intent.getIntExtra("courtId", 0)
        val courtName = intent.getStringExtra("courtName")
        val sport = intent.getStringExtra("sport")

        reservation = Reservation(reservationId,courtId,numOfPlayers,price, LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time))
        court = Court(courtId, courtName!!, sport!!, 0)
        updateContent()

        cancelButton.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    db.playerReservationDAO().deletePlayerReservationByReservationId(reservationId)
                    db.reservationDao().updateNumOfPlayers(reservationId, -1)
                    val result: Intent = Intent()
                    result.putExtra("result", true)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                } catch (err: RuntimeException) {
                    Log.e("cancel", err.toString())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Unable to cancel your reservation.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun updateContent() {
        sport_name.text = "${court.sport}"
        court_name_cancel_reservation.text = "${court.name}"
        location_cancel_reservation.text = "Via Giovanni Magni, 32"
        val dayMonth = reservation.date.format(DateTimeFormatter.ofPattern("dd MMM")).split(" ")
        val day = dayMonth[0]
        val month = dayMonth[1].replaceFirstChar { it.uppercase() }
        val formattedDate = "$day $month"
        date_cancel_reservation.text = formattedDate
        time_cancel_reservation.text = reservation.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }
}