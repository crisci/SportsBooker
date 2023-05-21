package com.example.lab2

import android.graphics.Picture
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.MyReservationsVM
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Equipment
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {


    lateinit var reservationVM: MyReservationsVM

    private lateinit var backButton: ImageView

    private lateinit var db: ReservationAppDatabase
    private lateinit var sport: TextView
    private lateinit var court: TextView
    private lateinit var location: TextView
    private lateinit var hour: TextView
    private lateinit var price: TextView
    private lateinit var yourEquipments: TextView
    private lateinit var description: TextView
    private lateinit var courtPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        reservationVM = ViewModelProvider(this)[MyReservationsVM::class.java]

        db = ReservationAppDatabase.getDatabase(this)
        sport = findViewById(R.id.sport_name_detail_reservation)
        court = findViewById(R.id.court_name_detail_reservation)
        location = findViewById(R.id.location_detail_reservation)
        hour = findViewById(R.id.hour_detail)
        price = findViewById(R.id.price_detail)
        yourEquipments = findViewById(R.id.your_equipments_detail)
        description = findViewById(R.id.court_description_detail)
        courtPhoto = findViewById(R.id.court_image)

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Details"


        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val reservationId = intent.getIntExtra("reservationId", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val reservation = reservationVM.getReservationDetails(reservationId)
            MainScope().launch {
                updateView(reservation)
            }
        }

    }

    private fun updateView(reservation: ReservationWithCourtAndEquipments) {
        sport.text = reservation.court.sport
        court.text = reservation.court.name
        location.text = "Via Giovanni Magni, 32"
        hour.text = reservation.reservation.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        price.text = "€${String.format("%.02f", reservation.finalPrice)}"
        description.text = reservation.court.description
        courtPhoto.setImageBitmap(reservation.court.courtPhoto)
        setupEquipments(reservation.equipments)
    }

    private fun setupEquipments(equipmets: List<Equipment>) {
       if(equipmets.isNotEmpty()) {
           val equipmentDetails = findViewById<LinearLayout>(R.id.equipments_container_detail)
           for(e in equipmets) {
               val equipmentView = MaterialTextView(this)
               equipmentView.text = e.name
               equipmentView.textSize = 16f
               equipmentView.setPadding(0,16, 0, 0)
               equipmentDetails.addView(equipmentView)
           }
       } else {
           yourEquipments.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
           yourEquipments.text = "You don't have any equipment booked"
       }
    }
}


