package com.example.lab2.calendar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.lab2.R
import com.example.lab2.databinding.WeekCalendarCalendarDayBinding
import com.example.lab2.databinding.WeekCalendarFragmentBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class WeekCalendarCreateMatch : Fragment(R.layout.calendar_create_match){

    private lateinit var selectedDate : LocalDate //current date
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    lateinit var vm: CalendarVM

    private lateinit var timeslotFilterButton: TextView
    private lateinit var oldDate: LocalDate

    private lateinit var exSevenCalendar: WeekCalendarView
    private lateinit var exSevenToolbar: Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exSevenCalendar = view.findViewById(R.id.exSevenCalendar)
        exSevenToolbar = view.findViewById(R.id.exSevenToolbar)


        vm = ViewModelProvider(requireActivity())[CalendarVM::class.java]

        vm.getSelectedDate().observe(viewLifecycleOwner){
            oldDate = selectedDate
            selectedDate = it
            exSevenCalendar.scrollToWeek(selectedDate)
            updateDate()
        }

        selectedDate = vm.getSelectedDate().value!!



        /* DayViewContainer is our view container which acts as a view holder
           for each date cell.
        *  The view passed in here is the inflated day view resource
        *  which we provided.
        * */
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = WeekCalendarCalendarDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date && day.date >= LocalDate.now()) {
                        oldDate = selectedDate
                        Log.d("dateInsideWeekCalendar",day.date.toString())
                        vm.setSelectedDate(day.date)
                        updateDate()
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                bind.exSevenDateText.text = dateFormatter.format(day.date)
                bind.exSevenDayText.text = day.date.dayOfWeek.displayText()

                val colorRes = when {
                    day.date == selectedDate -> R.color.darker_blue
                    day.date < LocalDate.now() -> R.color.darker_blue_disabled
                    else -> R.color.darker_blue
                }

                bind.exSevenDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenDayText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenSelectedView.isVisible = day.date == selectedDate
            }
        }

        // Provide a WeekDayBinder for the CalendarView using your DayViewContainer type.
        exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            // // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        exSevenCalendar.weekScrollListener = { weekDays ->
            exSevenToolbar.title = getWeekPageTitle(weekDays)
            exSevenToolbar.setTitleTextColor(view.context.getColorCompat(R.color.darker_blue))
            exSevenToolbar.setTitleTextAppearance(view.context,R.style.MonthBoldTextAppearance)
        }

        val currentMonth = YearMonth.now()
        exSevenCalendar.setup(
            LocalDate.now(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            LocalDate.now().dayOfWeek,
        )
        exSevenCalendar.scrollToDate(LocalDate.now())
    }

    fun updateDate(){
        exSevenCalendar.notifyDateChanged(selectedDate)
        oldDate?.let { exSevenCalendar.notifyDateChanged(it) }
    }
}
