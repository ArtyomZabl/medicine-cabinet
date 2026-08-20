package com.example.android.medicinecabinet.detail.editSchedule

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.detail.editSchedule.ui.AutoCompleteTextFieldDays
import com.example.android.medicinecabinet.detail.editSchedule.ui.AutoCompleteTextFieldIntake
import com.example.android.medicinecabinet.utils.CardBackgroundDark
import com.example.android.medicinecabinet.utils.CardBackgroundLight
import com.example.android.medicinecabinet.utils.IntakeInterval
import com.example.android.medicinecabinet.utils.WeekDay
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

class EditScheduleFragment : Fragment() {

    private val editScheduleViewModel: EditScheduleViewModel by viewModels {
        EditScheduleViewModelFactory(
            MedicineRepository(
                MedicineDatabase.getDatabase(requireContext()).medicineDao(),
                MedicineDatabase.getDatabase(requireContext()).takingTimeDao(),
                MedicineDatabase.getDatabase(requireContext()).selectedTakingDaysDao(),
                MedicineDatabase.getDatabase(requireContext()).medicineLogDao()
            )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    editScheduleViewModel.navBack.collect {
                        findNavController().navigateUp()
                    }
                }
            }

            fun showTimePicker(onTimeSelected: (time: String) -> Unit) {
                val isSystem24Hour = is24HourFormat(context)
                val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

                val picker =
                    MaterialTimePicker.Builder()
                        .setTitleText("SELECT YOUR TIMING")
                        .setTimeFormat(clockFormat)
                        .setHour(LocalDateTime.now().hour)
                        .setMinute(LocalDateTime.now().minute)
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

            setContent {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "Изменить график") },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        editScheduleViewModel.onNavBack()
                                    },
                                    content = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "ArrowBack"
                                        )
                                    }
                                )
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        )
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        EditScheduleScreen(
                            editScheduleViewModel,
                            showTimePicker = {
                                showTimePicker { time ->
                                    editScheduleViewModel.addTakingTimes(time)
                                }
                            },
                            showUpdateTimePicker = { takingTime ->
                                showTimePicker { time ->
                                    editScheduleViewModel.updateTakingTime(
                                        takingTime = takingTime,
                                        time = time
                                    )
                                }
                            }
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun EditScheduleScreen(
    editScheduleViewModel: EditScheduleViewModel,
    showTimePicker: () -> Unit,
    showUpdateTimePicker: (TakingTime) -> Unit
) {
    val medicine by editScheduleViewModel.medicine.observeAsState()
    val takingTimes by editScheduleViewModel.takingTimes.observeAsState(emptyList())
    val scrollState = rememberScrollState()

    if (medicine == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(CardBackgroundLight)
            ) {
                IntakeInterval(editScheduleViewModel, medicine!!)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Время приёма",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(CardBackgroundLight)
            ) {
                TimeSelectionSection(
                    editScheduleViewModel,
                    takingTimes,
                    showTimePicker = showTimePicker,
                    showUpdateTimePicker = { takingTime -> showUpdateTimePicker(takingTime) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Длительность",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(CardBackgroundLight)
            ) {
                DurationSection()
            }
        }

    }
}

@Composable
fun IntakeInterval(
    editScheduleViewModel: EditScheduleViewModel,
    medicine: Medicine
) {
    var intakeInterval by remember { mutableStateOf(medicine.intakeInterval) }
    val selectedDaysString by editScheduleViewModel.daysIntervalString.observeAsState("Через день")
    val selectedDays = editScheduleViewModel.selectedDays.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AutoCompleteTextFieldIntake(
            selectedInterval = intakeInterval,
            onIntervalSelected = {
                intakeInterval = it
            }
        )
        if (intakeInterval == IntakeInterval.EVERY_X_DAYS) {
            AutoCompleteTextFieldDays(
                selectedInterval = selectedDaysString,
                onIntervalSelected = {
                    editScheduleViewModel.setDaysInterval(it)
                },
                editScheduleViewModel = editScheduleViewModel
            )
        }

        if (intakeInterval == IntakeInterval.SPECIFIC_DAYS) {
            WeekDaySelector(
                editScheduleViewModel = editScheduleViewModel,
                selectedDays = selectedDays.value
            )
        }

    }

}


@Composable
fun WeekDaySelector(
    editScheduleViewModel: EditScheduleViewModel,
    selectedDays: List<WeekDay>?
) {
    Log.d("WeekDaySelector", "WeekDaySelector called ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeekDay.entries.forEach { day ->
            val isSelected = selectedDays?.any {
                Log.d("WeekDaySelector", "it.weekDay: $it day: $day")
                it == day
            } ?: false

            val dayLetter = when (day) {
                WeekDay.MON -> "П"
                WeekDay.TUE -> "В"
                WeekDay.WED -> "С"
                WeekDay.THU -> "Ч"
                WeekDay.FRI -> "П"
                WeekDay.SAT -> "С"
                WeekDay.SUN -> "В"
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colorResource(R.color.frosty_sky)
                        else colorResource(R.color.light_grey)
                    )
                    .clickable {
                        editScheduleViewModel.toggleDay(day)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayLetter,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
            }
        }
    }
}


@Composable
fun TimeSelectionSection(
    editScheduleViewModel: EditScheduleViewModel,
    takingTimes: List<TakingTime>,
    showTimePicker: () -> Unit,
    showUpdateTimePicker: (TakingTime) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column() {
            val sortedTakingTimes = remember(takingTimes) {
                takingTimes.sortedBy { LocalTime.parse(it.time) }
            }
            sortedTakingTimes.forEachIndexed { index, takingTime ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(CardBackgroundDark),
                        onClick = { showUpdateTimePicker(takingTime) }
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                            text = takingTime.time,
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }


                    Box(
                        modifier = Modifier
                            .size(24.dp) // Размер всего круга
                            .clip(CircleShape)
                            .background(Color.Red) // Красный фон
                            .clickable {
                                editScheduleViewModel.deleteTakingTime(takingTime)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Удалить",
                            tint = Color.White, // Белый минус
                            modifier = Modifier.size(18.dp) // Размер самого минуса внутри
                        )
                    }
                }

                if (index < takingTimes.lastIndex) {
                    HorizontalDivider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }
        }

        Button(
            onClick = showTimePicker,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Добавить время")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun DurationSection() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorResource(R.color.cv_background_light),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {

                // Date start
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "ДАТА НАЧАЛА",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colorResource(R.color.cv_background_light)
                    ) {
                        Text(
                            "1 января (сегодня)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                //Date end
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "ДАТА ОКОНЧАНИЯ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Изменить",
                    style = TextStyle(
                        fontSize = 22.sp
                    )
                )
            }
        }
    }

}

