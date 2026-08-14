package com.example.android.medicinecabinet.detail.editDetail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.utils.CardBackgroundLight
import com.example.android.medicinecabinet.utils.DateFormatter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EditDetailFragment : Fragment() {

    private val editDetailViewModel: EditDetailViewModel by viewModels {
        EditDetailViewModelFactory(
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
                    editDetailViewModel.navBack.collect {
                        findNavController().navigateUp()
                    }
                }
            }

            setContent {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(text = "Редактирование")
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        editDetailViewModel.onNavBack()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "NavBack"
                                    )
                                }
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White)
                    ) {
                        EditDetailScreen(editDetailViewModel = editDetailViewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun EditDetailScreen(editDetailViewModel: EditDetailViewModel) {
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            FirstCard(editDetailViewModel)
            SecondCard(editDetailViewModel)
        }
    }
}

@Composable
fun FirstCard(editDetailViewModel: EditDetailViewModel) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(CardBackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Название",
                style = TextStyle(color = Color.Black, fontSize = 16.sp)
            )
            val nameText = editDetailViewModel.nameMeds.observeAsState()
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = nameText.value ?: "",
                onValueChange = { newValue ->
                    editDetailViewModel.updateMedsName(newValue)
                },
                placeholder = {
                    Text("Введите название...", color = Color.LightGray)
                },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.LightGray,
                    unfocusedContainerColor = Color.LightGray,
                ),
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Количество",
                style = TextStyle(color = Color.Black, fontSize = 16.sp)
            )

            val quantityText = editDetailViewModel.quantityMeds.observeAsState()
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = quantityText.value?.toString() ?: "",
                onValueChange = { newValue ->
                    editDetailViewModel.updateMedsQuantity(newValue.toIntOrNull() ?: 0)
                },
                placeholder = {
                    Text("Введите количество...", color = Color.LightGray)
                },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.LightGray,
                    unfocusedContainerColor = Color.LightGray,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Годен до",
                style = TextStyle(color = Color.Black, fontSize = 16.sp)
            )

            val expirationDateText = editDetailViewModel.expirationDateMeds.observeAsState()
            val formattedDate = if (!expirationDateText.value.isNullOrBlank()) {
                try {
                    DateFormatter.fullUi(LocalDate.parse(expirationDateText.value))
                } catch (e: Exception) {
                    expirationDateText.value
                }
            } else {
                ""
            }

            val showDatePicker = remember { mutableStateOf(false) }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable {
                        showDatePicker.value = true
                    },
                value = formattedDate ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = {
                    Text("00.00.0000", color = Color.LightGray)
                },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Black, // Текст остается черным
                    disabledContainerColor = Color.LightGray,
                    disabledIndicatorColor = Color.Transparent,
                    disabledPlaceholderColor = Color.LightGray
                ),
            )

            if (showDatePicker.value) {
                DatePickerModal(
                    onDismiss = {
                        showDatePicker.value = false
                    },
                    onDateSelected = { date ->
                        editDetailViewModel.updateMedsExpDate(date)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDate = datePickerState.selectedDateMillis
                if (selectedDate != null) {
                    val date = Instant.ofEpochMilli(selectedDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    onDateSelected(date.toString())
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun SecondCard(editDetailViewModel: EditDetailViewModel) {
    val descriptionText = editDetailViewModel.descriptionMeds.observeAsState()
    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Описание",
                fontSize = 16.sp,
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = descriptionText.value ?: "",
                onValueChange = {newValue ->
                    editDetailViewModel.updateMedsDescription(newValue)
                },
                placeholder = {
                    Text("Введите описание...", color = Color.Gray)
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.LightGray,
                    unfocusedContainerColor = Color.LightGray,
                ),
                singleLine = false,

                )

        }

    }
}