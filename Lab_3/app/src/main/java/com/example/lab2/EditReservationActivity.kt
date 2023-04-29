package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.court.Court
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationWithCourt
import com.example.lab2.database.reservation.formatPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EditReservationActivity : AppCompatActivity() {

    private lateinit var res : Reservation
    private lateinit var reservation:ReservationWithCourt
    private lateinit var court_name_edit_reservation: TextView
    private lateinit var location_edit_reservation: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var db: ReservationAppDatabase
    private lateinit var backButton: ImageView
    private lateinit var priceText: TextView
    private lateinit var sport_name: TextView

    @Inject
    lateinit var vm: CalendarViewModel

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

        val reservationId = intent.getIntExtra("reservationId", 0)
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val numOfPlayers = intent.getIntExtra("numOfPlayers", 0)
        val price = intent.getDoubleExtra("price", 0.0)
        val courtId = intent.getIntExtra("courtId", 0)
        val courtName = intent.getStringExtra("courtName")
        val sport = intent.getStringExtra("sport")


        res = Reservation(reservationId,courtId,numOfPlayers,price,LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.parse(time))
        reservation = ReservationWithCourt(res, Court(courtId, courtName!!, sport!!))
        updateContent()

        /*supportActionBar?.title = "Edit Reservation $reservationId"
        supportActionBar?.elevation = 0f*/

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

            CoroutineScope(Dispatchers.IO).launch {
                try{
                    res.time = res.time.plusHours(1)
                    db.reservationDao().saveReservation(res)
                    setResult(Activity.RESULT_OK)
                    finish()
                }catch(err: RuntimeException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Unable to update your reservation.", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }

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
        sport_name.text = "${reservation.court.sport}"
        court_name_edit_reservation.text = "${reservation.court.name}"
        location_edit_reservation.text = "Via Giovanni Magni, 32"
        priceText.text = "You will pay €${reservation.reservation.formatPrice()} locally."
    }

}