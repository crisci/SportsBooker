package com.example.lab2

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.ViewModelProvider
import com.example.lab2.viewmodels.MyReservationsVM
import com.example.lab2.calendar.displayText
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments
import com.example.lab2.entities.Equipment
import com.example.lab2.viewmodels_firebase.DetailsViewModel
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.example.lab2.viewmodels_firebase.firebaseToMatchWithCourtAndEquipments
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {


    lateinit var reservationVM: MyReservationsVM
    lateinit var detailsViewModel: DetailsViewModel

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
    private lateinit var rating: RatingBar
    private lateinit var date: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        reservationVM = ViewModelProvider(this)[MyReservationsVM::class.java]
        detailsViewModel = ViewModelProvider(this)[DetailsViewModel::class.java]

        db = ReservationAppDatabase.getDatabase(this)
        sport = findViewById(R.id.sport_name_detail_reservation)
        court = findViewById(R.id.court_name_detail_reservation)
        location = findViewById(R.id.location_detail_reservation)
        hour = findViewById(R.id.hour_detail)
        price = findViewById(R.id.price_detail)
        yourEquipments = findViewById(R.id.your_equipments_detail)
        description = findViewById(R.id.court_description_detail)
        courtPhoto = findViewById(R.id.court_image)
        rating = findViewById(R.id.detail_rating)
        date = findViewById(R.id.date_detail)

        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Details"


        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val reservationId = intent.getStringExtra("reservationId")!!
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("reservations").whereEqualTo(FieldPath.documentId(), reservationId).get().addOnSuccessListener { r ->
                Log.e("id", r.first().id.toString())
                FirebaseFirestore.getInstance().collection("matches").whereEqualTo(FieldPath.documentId(), r.first().getDocumentReference("match")?.id).get().addOnSuccessListener { m ->
                    Log.e("id", m.first().id.toString())
                    FirebaseFirestore.getInstance().collection("courts").whereEqualTo(FieldPath.documentId(), m.first().getDocumentReference("court")?.id).get().addOnSuccessListener { c ->
                        Log.e("id", c.first().id.toString())
                        val details = firebaseToMatchWithCourtAndEquipments(m.first(), c.first(), r.first())
                        MainScope().launch {
                            updateView(details, 0.0)
                        }
                    }
                }
            }
        }

    }

    private fun updateView(reservation: MatchWithCourtAndEquipments, avg: Double) {
        sport.text = reservation.court.sport
        court.text = reservation.court.name
        location.text = "Via Giovanni Magni, 32"
        hour.text = reservation.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        date.text = setupDate(reservation.match.date)
        price.text = "€${String.format("%.02f", reservation.finalPrice)}"
        description.text = reservation.court.description
        //courtPhoto.setImageBitmap(reservation.court.courtPhoto)
        rating.rating = avg.toFloat()
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

    private fun setupDate(date: LocalDate): String {
        return "${date.dayOfWeek.displayText()} ${date.format(DateTimeFormatter.ofPattern("dd"))} ${date.month.displayText()}"
    }
}


