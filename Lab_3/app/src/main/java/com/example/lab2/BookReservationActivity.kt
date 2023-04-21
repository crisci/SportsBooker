package com.example.lab2

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.lab2.calendar.WeekCalendar
import com.example.lab2.databinding.BookReservationActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookReservationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = BookReservationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val calendarFragment = WeekCalendar()
        supportFragmentManager.beginTransaction()
            .add(R.id.homeContainer,calendarFragment,calendarFragment.javaClass.simpleName)
            .addToBackStack(calendarFragment.javaClass.simpleName)
            .commit()

    }
}
