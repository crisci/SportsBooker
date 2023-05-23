package com.example.lab2

import android.app.Activity
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.lab2.calendar.CalendarVM
import com.example.lab2.calendar.CreateMatchVM
import com.example.lab2.calendar.UserViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class CreateMatchActivity : AppCompatActivity() {

    var invalidTime = false

    private lateinit var timeAutoCompleteTextView: AutoCompleteTextView
    private lateinit var sportAutoCompleteTV: AutoCompleteTextView
    private lateinit var confirmButton: Button

    lateinit var calendarVM: CalendarVM
    @Inject
    lateinit var userVM: UserViewModel

    lateinit var createMatchVM: CreateMatchVM
    private lateinit var db: ReservationAppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match)

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar_with_profile)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Create a match"

        db = ReservationAppDatabase.getDatabase(this)

        calendarVM = ViewModelProvider(this)[CalendarVM::class.java]
        userVM = ViewModelProvider(this)[UserViewModel::class.java]
        createMatchVM = ViewModelProvider(this)[CreateMatchVM::class.java]

        timeAutoCompleteTextView = findViewById(R.id.autoCompleteTextView2)
        sportAutoCompleteTV = findViewById(R.id.autoCompleteTextView)
        confirmButton = findViewById(R.id.confirm_button_confirm_reservation)

        val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, userVM.getUser().value!!.interests)
        sportAutoCompleteTV.setAdapter(arrayAdapter)

        val arrayAdapter2 = ArrayAdapter(applicationContext, R.layout.dropdown_item, createMatchVM.getListTimeslots().value!!)
        timeAutoCompleteTextView.setAdapter(arrayAdapter2)

        calendarVM.getSelectedDate().observe(this) {
            createMatchVM.filterTimeslots(it)
            val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, createMatchVM.getListTimeslots().value!!)
            timeAutoCompleteTextView.setAdapter(arrayAdapter)
        }

        confirmButton.setOnClickListener {
            val formattedSport = sportAutoCompleteTV.text.toString().lowercase().replaceFirstChar { it.uppercase() }
            val time = LocalTime.parse(timeAutoCompleteTextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
            createMatchVM.createMatch(
                calendarVM.getSelectedDate().value!!,
                time,
                formattedSport
            )
        }

        createMatchVM.getExceptionMessage().observe(this) {
            if(it == "Match created successfully") {
                setResult(Activity.RESULT_OK)
                finish()
            }
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_item, userVM.getUser().value!!.interests)
        sportAutoCompleteTV.setAdapter(arrayAdapter)
        val arrayAdapter2 = ArrayAdapter(applicationContext, R.layout.dropdown_item, createMatchVM.getListTimeslots().value!!)
        timeAutoCompleteTextView.setAdapter(arrayAdapter2)
    }

}