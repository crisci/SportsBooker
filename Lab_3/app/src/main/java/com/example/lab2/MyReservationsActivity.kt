package com.example.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.CalendarViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.reservation.Reservation
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class MyReservationsActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var myProfileButton: ImageView
    private lateinit var db: ReservationAppDatabase

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reservations)
        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar_with_profile)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "My Reservations"

        db = ReservationAppDatabase.getDatabase(this)

        navController = (
            supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        ).navController

        myProfileButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_my_profile)!!
        myProfileButton.setOnClickListener {
            val intentShowProfile = Intent(this, ShowProfileActivity::class.java)
            launcher.launch(intentShowProfile)
        }

        // IGNORE: DB gets created the very first time only if some Dao operations are executed

    }
}