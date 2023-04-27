package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.reservation.Reservation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EditReservationActivity : AppCompatActivity() {

    private lateinit var reservation:Reservation
    private lateinit var court_name_edit_reservation: TextView
    private lateinit var location_edit_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var db: ReservationAppDatabase

    @Inject
    lateinit var vm: CalendarViewModel

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            val data: Intent? = response.data
            val cancel = data?.getBooleanExtra("result", false)
            if(cancel == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    db.reservationDao().cancelReservationById(reservation.reservationId)
                }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reservation)

        db = ReservationAppDatabase.getDatabase(this)
        court_name_edit_reservation = findViewById(R.id.court_name_edit_reservation)
        location_edit_reservation = findViewById(R.id.location_edit_reservation)
        cancelButton = findViewById(R.id.cancel_button_edit_reservation)
        cancelButton.setOnClickListener {
            val intent = Intent(this, CancelReservationActivity::class.java).apply {
                putExtra("reservationId", reservation.reservationId)
                putExtra("courtId", reservation.courtId)
                putExtra("date", reservation.date.toString())
                putExtra("time", reservation.time.toString())
                putExtra("price", reservation.price)
                putExtra("numOfPlayers", reservation.numOfPlayers)
            }
            launcher.launch(intent)
        }

        val reservationId = intent.getIntExtra("reservationId", 0)
        val courtId = intent.getIntExtra("courtId", 0)
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)


        //TODO: fetch the reservation from the DB using the reservationId
        reservation = Reservation(reservationId,courtId,numOfPlayers,price, LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.now())
        updateContent()

        supportActionBar?.title = "Edit Reservation $reservationId"
        supportActionBar?.elevation = 0f
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO: Serialization of reservation
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //TODO: fromJSON
        updateContent()
    }

    private fun updateContent() {
        court_name_edit_reservation.text = "Campo ${reservation.courtId}"
        location_edit_reservation.text = "Corso Duca degli Abruzzi 24, Torino"
    }

}