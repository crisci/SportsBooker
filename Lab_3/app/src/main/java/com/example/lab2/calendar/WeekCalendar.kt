package com.example.lab2.calendar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
import com.kizitonwose.calendar.view.WeekDayBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class WeekCalendar : Fragment(R.layout.week_calendar_fragment){

    private lateinit var selectedDate : LocalDate //current date
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private lateinit var binding: WeekCalendarFragmentBinding

    lateinit var vm: CalendarVM


    private lateinit var roundCalendarButton : ImageButton
    private lateinit var timeslotFilterButton: TextView
    private lateinit var oldDate: LocalDate
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        binding = WeekCalendarFragmentBinding.bind(view)

        vm = ViewModelProvider(requireActivity())[CalendarVM::class.java]

        vm.getSelectedDate().observe(viewLifecycleOwner){
            oldDate = selectedDate
            selectedDate = it
            binding.exSevenCalendar.scrollToWeek(selectedDate)
            updateDate()
        }

        selectedDate = vm.getSelectedDate().value!!

        roundCalendarButton = binding.roundCalendarButton
        roundCalendarButton.setOnClickListener {
            navController.navigate(R.id.action_global_to_fullScreenCalendar)
        }

        timeslotFilterButton = binding.timeslotFilter
        timeslotFilterButton.setOnClickListener {
            val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Pick your time")
                .setHour(vm.selectedTime.value!!.hour)
                .setMinute(vm.selectedTime.value!!.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(INPUT_MODE_CLOCK)
                .build()

            materialTimePicker.show(childFragmentManager,requireActivity().toString())

            materialTimePicker.addOnPositiveButtonClickListener {

                val pickedHour: Int = materialTimePicker.hour
                val pickedMinute: Int = materialTimePicker.minute

                vm.selectedTime.value = LocalTime.of(pickedHour,pickedMinute)

            }
        }

        vm.selectedTime.observe(viewLifecycleOwner) {
            timeslotFilterButton.text = it.format(DateTimeFormatter.ofPattern("HH:mm"))
        }



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
        binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            // // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        binding.exSevenCalendar.weekScrollListener = { weekDays ->
            binding.exSevenToolbar.title = getWeekPageTitle(weekDays)
            binding.exSevenToolbar.setTitleTextColor(view.context.getColorCompat(R.color.darker_blue))
            binding.exSevenToolbar.setTitleTextAppearance(view.context,R.style.MonthBoldTextAppearance)
        }

        val currentMonth = YearMonth.now()
        binding.exSevenCalendar.setup(
            LocalDate.now(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            LocalDate.now().dayOfWeek,
        )
        binding.exSevenCalendar.scrollToDate(LocalDate.now())
    }

    fun updateDate(){
        binding.exSevenCalendar.notifyDateChanged(selectedDate)
        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
    }
}
