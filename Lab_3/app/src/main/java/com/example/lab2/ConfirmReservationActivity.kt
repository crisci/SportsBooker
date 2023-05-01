package com.example.lab2

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.formatPrice
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class ConfirmReservationActivity : AppCompatActivity() {

    private lateinit var db: ReservationAppDatabase
    private lateinit var reservation: Reservation
    private lateinit var court: Court
    private lateinit var sport_name: TextView
    private lateinit var court_name_confirm_reservation: TextView
    private lateinit var location_confirm_reservation: TextView
    private lateinit var date_confirm_reservation: TextView
    private lateinit var time_confirm_reservation: TextView
    private lateinit var confirmButton: Button
    private lateinit var priceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_booking)

        db = ReservationAppDatabase.getDatabase(this)
        sport_name = findViewById(R.id.sport_name_confirm_reservation)
        court_name_confirm_reservation = findViewById(R.id.court_name_confirm_reservation)
        location_confirm_reservation = findViewById(R.id.location_confirm_reservation)
        date_confirm_reservation = findViewById(R.id.date_confirm_reservation)
        time_confirm_reservation = findViewById(R.id.time_confirm_reservation)
        confirmButton = findViewById(R.id.confirm_button_confirm_reservation)
        priceText = findViewById(R.id.local_price_confirm_reservation2)


        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Confirm Reservation"


        val reservationId = intent.extras?.getInt("reservationId", 0)
        val date = intent.extras?.getString("date")
        val time = intent.extras?.getString("time")
        val numOfPlayers = intent.extras?.getInt("numOfPlayers", 0)
        val price = intent.extras?.getDouble("price", 0.0)
        val courtId = intent.extras?.getInt("courtId", 0)
        val courtName = intent.extras?.getString("courtName")
        val sport = intent.extras?.getString("sport")
        val maxNumberOfPlayers = intent.extras?.getInt("maxNumberOfPlayers")

        reservation = Reservation(reservationId!!,courtId!!,numOfPlayers!!,price!!, LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time))
        court = Court(courtId, courtName!!, sport!!, maxNumberOfPlayers!!)

        updateContent()
    }

    private fun updateContent() {
        sport_name.text = "${court.sport}"
        court_name_confirm_reservation.text = "${court.name}"
        location_confirm_reservation.text = "Via Giovanni Magni, 32"
        val dayMonth = reservation.date.format(DateTimeFormatter.ofPattern("dd MMM")).split(" ")
        val day = dayMonth[0]
        val month = dayMonth[1].replaceFirstChar { it.uppercase() }
        val formattedDate = "$day $month"
        date_confirm_reservation.text = formattedDate
        time_confirm_reservation.text = reservation.time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
        priceText.text = "You will pay €${reservation.formatPrice()} locally."
        confirmButton.setOnClickListener {
            thread {
                if(reservation.numOfPlayers < court.maxNumOfPlayers) {
                    db.playerReservationDAO().confirmReservation(1, reservation.reservationId)
                    db.reservationDao().updateNumOfPlayers(reservation.reservationId)
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    //TODO: Toast notify error
                }
            }
        }
    }
}