package com.example.android.medicinecabinet.homeScreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.medicineLog.MedicineLog
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.utils.CardBackgroundLight
import java.time.LocalDate


class DialogChangeTakenState(
    private val medicine: Medicine,
    private val allTimes: List<TakingTime>,
    private val onClickTaken: () -> Unit,
    private val homeScreenViewModel: HomeScreenViewModel
    ) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                DialogScreen(
                    medicine = medicine,
                    allTimes = allTimes,
                    homeScreenViewModel = homeScreenViewModel,
                    onDismissRequest = { dismiss() },
                    onClickTaken = { onClickTaken }
                )
            }
        }
    }
}

@Composable
fun DialogScreen(
    medicine: Medicine,
    allTimes: List<TakingTime>,
    homeScreenViewModel: HomeScreenViewModel,
    onDismissRequest: () -> Unit,
    onClickTaken: () -> Unit
) {
    val medsLogByDate by homeScreenViewModel.medsLogByDate.observeAsState(emptyList())
    LaunchedEffect(medicine.medicineId) {
        homeScreenViewModel.getThisMedsLogByDate(medicine.medicineId, LocalDate.now().toString())
    }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    AsyncImage(
                        model = medicine.imagePath,
                        contentDescription = "image",
                        modifier = Modifier
                            .size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = medicine.name,
                        style = TextStyle(fontSize = 20.sp)
                    )
                }

                Log.d("LocalDate", "LocalDate: ${LocalDate.now()}")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allTimes) {time ->
                        val currentLog = medsLogByDate.find { it.takingTimeId == time.id }
                        Log.d("currentLog", "currentLog: $currentLog")
                        val isTaken = currentLog?.isTaken ?: false
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(CardBackgroundLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = time.time,
                                    style = TextStyle(fontSize = 26.sp)
                                )
                                Button(
                                    colors = ButtonDefaults.buttonColors(Color.LightGray),
                                    onClick = {
                                        if (currentLog != null) {
                                            homeScreenViewModel.updateIsTakenState(currentLog.logId, !isTaken)
                                        } else {
                                            val newMedsLog = MedicineLog(
                                                dateTaken = LocalDate.now().toString(),
                                                medicineId = medicine.medicineId,
                                                takingTimeId = time.id,
                                                isTaken = true
                                            )
                                            homeScreenViewModel.insertNewData(newMedsLog)
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(if (!isTaken) R.drawable.ic_correct else R.drawable.ic_close),
                                            contentDescription = "image",
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            text = if(!isTaken) "Принято" else "Отменить",
                                            style = TextStyle(color = Color.Black)
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

}