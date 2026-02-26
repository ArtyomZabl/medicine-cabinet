package com.example.android.medicinecabinet.homeScreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.MedicineDatabase
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.utils.CardBackgroundLight
import java.time.LocalDate
import java.time.LocalTime

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val repository = MedicineRepository(
            MedicineDatabase.getDatabase(requireContext()).medicineDao(),
            MedicineDatabase.getDatabase(requireContext()).takingTimeDao(),
            MedicineDatabase.getDatabase(requireContext()).selectedTakingDaysDao(),
            MedicineDatabase.getDatabase(requireContext()).medicineLogDao()
        )
        val factory = HomeScreenViewModelFactory(repository)
        val homeScreenViewModel = ViewModelProvider(this, factory)[HomeScreenViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                HomeScreen(
                    onNavigateMeds = {
                        findNavController().navigate(R.id.action_homeFragment_to_detailFragment)
                    },
                    homeScreenViewModel = homeScreenViewModel
                )
            }
        }
    }
}


@Composable
fun HomeScreen(
    onNavigateMeds: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel
) {
    val rawMedicines by homeScreenViewModel.allTakingMedicines.observeAsState(emptyList())
    val medicines = remember(rawMedicines) { rawMedicines.reversed() }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Принимаемые лекарства")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(medicines) { medicine ->
                val allTimes by homeScreenViewModel.getTimesThisMeds(medicine.medicineId)
                    .observeAsState(emptyList())
                val allTimesSorted = remember(allTimes) { allTimes.sortedBy { it.time } }

                if (allTimesSorted.isNotEmpty()) {

                    val medsLogByDate by homeScreenViewModel.getThisMedsLogByDate(
                        medicine.medicineId,
                        LocalDate.now().toString()
                    ).observeAsState()
                    Log.d("LocalDate", "LocalDate: ${LocalDate.now()}")
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            if (!medicine.imagePath.isNullOrEmpty()) {
                                AsyncImage(
                                    medicine.imagePath,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(end = 8.dp),
                                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = medicine.name,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(allTimesSorted) { time ->
                                        val isTaken = medsLogByDate?.any { log -> log.takingTimeId == time.id} == true
                                        val localTime = LocalTime.now().toString()
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                if(localTime <= time.time) {
                                                    CardBackgroundLight
                                                } else {
                                                    if (isTaken) Color.Green else Color.Red
                                                }
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(
                                                    vertical = 2.dp,
                                                    horizontal = 4.dp
                                                ),
                                                text = time.time,
                                                style = TextStyle(fontSize = 14.sp)
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
}
