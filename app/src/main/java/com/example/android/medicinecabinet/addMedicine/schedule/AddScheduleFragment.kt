package com.example.android.medicinecabinet.addMedicine.schedule

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.medicinecabinet.MainActivity
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.addMedicine.AddMedicineViewModel
import com.example.android.medicinecabinet.data.takingTime.TakingTimeListener
import com.example.android.medicinecabinet.data.takingTime.TakingTimeAdapter
import com.example.android.medicinecabinet.databinding.FragmentAdd3ScheduleBinding
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.utils.DateFormatter
import com.example.android.medicinecabinet.utils.Functions.setMarginStart
import com.example.android.medicinecabinet.utils.Functions.setMarginTop
import com.example.android.medicinecabinet.utils.TimeFormatter
import com.example.android.medicinecabinet.utils.WeekDay
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class AddScheduleFragment : Fragment() {

    lateinit var binding: FragmentAdd3ScheduleBinding
    private val addMedicineViewModel: AddMedicineViewModel by navGraphViewModels(R.id.nav_graph_meds) {
        (requireActivity() as MainActivity).factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add3_schedule, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addMedicineViewModel = addMedicineViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.tvDateStart.text = addMedicineViewModel.setTodayDate()

        fun showTimePicker(onTimeSelected: (time: String) -> Unit) {
            val isSystem24Hour = is24HourFormat(context)
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

            val picker =
                MaterialTimePicker.Builder()
                    .setTitleText("SELECT YOUR TIMING")
                    .setTimeFormat(clockFormat)
                    .setHour(12)
                    .setMinute(10)
                    .build()

            picker.addOnPositiveButtonClickListener {
                val pickedHour = picker.hour
                val pickedMinute = picker.minute
                val time = when {
                    pickedHour < 10 -> {
                        if (pickedMinute < 10) {
                            "0$pickedHour:0$pickedMinute"
                        } else {
                            "0$pickedHour:$pickedMinute"
                        }
                    }

                    else -> {
                        if (pickedMinute < 10) {
                            "$pickedHour:0$pickedMinute"
                        } else {
                            "$pickedHour:$pickedMinute"
                        }
                    }

                }
                onTimeSelected(time)
            }

            picker.show(parentFragmentManager, "tag")
        }

        fun setupDays() {
            val dayViews = listOf(
                binding.dayMon to WeekDay.MON,
                binding.dayTue to WeekDay.TUE,
                binding.dayWed to WeekDay.WED,
                binding.dayThu to WeekDay.THU,
                binding.dayFri to WeekDay.FRI,
                binding.daySat to WeekDay.SAT,
                binding.daySun to WeekDay.SUN,
            )

            dayViews.forEach { (view, day) ->
                view.setOnClickListener {
                    addMedicineViewModel.toggleDay(day)
                }
            }

            addMedicineViewModel.selectedDays.observe(viewLifecycleOwner) { selected ->
                dayViews.forEach { (view, day) ->
                    view.isSelected = selected.contains(day)
                }
            }
        }
        setupDays()

        val adapter = TakingTimeAdapter(
            TakingTimeListener(
                onItemClick = { takingTime ->
                    showTimePicker { time ->
                        addMedicineViewModel.updateTakingTime(takingTime, time)
                    }
                },
                onDeleteClick = { takingTime -> addMedicineViewModel.deleteTakingTime(takingTime) }
            )
        )
        binding.rcViewTakingTime.adapter = adapter
        binding.rcViewTakingTime.layoutManager = LinearLayoutManager(context)

        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_dropdown,
            addMedicineViewModel.intakeInterval
        )
        binding.autoCompleteIntakeInterval.setAdapter(arrayAdapter)

        val arrayAdapter1 = ArrayAdapter(
            requireContext(),
            R.layout.list_item_dropdown,
            addMedicineViewModel.displayIntervals
        )
        binding.autoCompleteDaysInterval.setAdapter(arrayAdapter1)

        if (addMedicineViewModel.takingTimes.value.isNullOrEmpty()){
            addMedicineViewModel.addTakingTime(TimeFormatter.short(LocalDateTime.now()))
        }

        addMedicineViewModel.selectedIntakeInterval.observe(viewLifecycleOwner) { selected ->
            if (selected != null && binding.autoCompleteIntakeInterval.text.toString() != selected) {
                binding.autoCompleteIntakeInterval.setText(selected, false)
            }

            when (selected) {
                "По мере необходимости" -> {
                    binding.textInputDaysInterval.visibility = View.GONE
                    binding.autoCompleteDaysInterval.setMarginTop(0)

                    binding.constraintLayoutTime.visibility = View.GONE

                    binding.constraintLayoutDuration.visibility = View.GONE
                }

                "Каждый день" -> {
                    binding.textInputDaysInterval.visibility = View.GONE
                    binding.textInputDaysInterval.setMarginTop(0)

                    binding.layoutWeekDays.visibility = View.GONE
                    binding.tvTakingTime.setMarginTop(0)

                    addMedicineViewModel.setSelectedStartTakingDate(DateFormatter.full(LocalDate.now()))
                    binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                    setupEndTakingDate()
                    binding.constraintLayoutTime.visibility = View.VISIBLE

                    binding.constraintLayoutDuration.visibility = View.VISIBLE
                }

                "В определённые дни" -> {
                    binding.textInputDaysInterval.visibility = View.GONE
                    binding.textInputDaysInterval.setMarginTop(0)

                    addMedicineViewModel.initWithTodayIfEmpty()
                    binding.tvTakingTime.setMarginTop(8)
                    binding.layoutWeekDays.visibility = View.VISIBLE

                    addMedicineViewModel.setSelectedStartTakingDate(DateFormatter.full(LocalDate.now()))
                    binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                    setupEndTakingDate()
                    binding.constraintLayoutTime.visibility = View.VISIBLE

                    binding.constraintLayoutDuration.visibility = View.VISIBLE
                }

                "Раз в несколько дней" -> {
                    binding.textInputDaysInterval.visibility = View.VISIBLE
                    binding.textInputDaysInterval.setMarginTop(8)

                    binding.tvTakingTime.setMarginTop(0)
                    binding.layoutWeekDays.visibility = View.GONE

                    binding.constraintLayoutTime.visibility = View.VISIBLE

                    addMedicineViewModel.setSelectedStartTakingDate(DateFormatter.full(LocalDate.now()))
                    binding.tvDateStart.text = addMedicineViewModel.selectedStartTakingDate.value
                    setupEndTakingDate()

                    binding.constraintLayoutDuration.visibility = View.VISIBLE
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.onClickAddTime.collect {
                    showTimePicker { addMedicineViewModel.addTakingTime(it) }
                }
            }
        }

        addMedicineViewModel.takingTimes.observe(viewLifecycleOwner) { times ->
            adapter.submitListByType(times, Constance.ITEM_TYPE_TIME_TAKING_EDIT)
        }

        addMedicineViewModel.isNextEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnNext.isEnabled = isEnabled
        }

        binding.autoCompleteIntakeInterval.setOnItemClickListener { _, _, position, _ ->
            addMedicineViewModel.setSelectedInterval(position)
        }

        binding.autoCompleteDaysInterval.setOnItemClickListener { _, _, position, _ ->
            val day = addMedicineViewModel.intervals[position]
            addMedicineViewModel.setDaysInterval(day)
        }

        addMedicineViewModel.daysInterval.observe(viewLifecycleOwner) { days ->
            binding.autoCompleteDaysInterval.setText(
                addMedicineViewModel.formatInterval(days),
                false
            )

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.navToResult.collect {
                    findNavController().navigate(R.id.action_addScheduleFragment_to_resultFragment)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addMedicineViewModel.changeSchedule.collect {
                    findNavController().navigate(R.id.action_addScheduleFragment_to_changeScheduleFragment)
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun setupEndTakingDate() {
        if (!addMedicineViewModel.selectedEndTakingDate.value.isNullOrBlank()) {
            binding.tvDateEnd.text = addMedicineViewModel.selectedEndTakingDate.value
            binding.tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.cvDateEnd.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cv_background_dark
                )
            )
            binding.tvDateEnd.setMarginStart(4)
        } else {
            binding.tvDateEnd.text = "Нет"
            binding.tvDateEnd.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey
                )
            )
            binding.cvDateEnd.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.cv_background_light
                )
            )
            binding.tvDateEnd.setMarginStart(0)
        }
    }

    override fun onResume() {
        super.onResume()
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_dropdown,
            addMedicineViewModel.intakeInterval
        )
        binding.autoCompleteIntakeInterval.setAdapter(arrayAdapter)

        val arrayAdapter1 = ArrayAdapter(
            requireContext(),
            R.layout.list_item_dropdown,
            addMedicineViewModel.displayIntervals
        )
        binding.autoCompleteDaysInterval.setAdapter(arrayAdapter1)
    }
}