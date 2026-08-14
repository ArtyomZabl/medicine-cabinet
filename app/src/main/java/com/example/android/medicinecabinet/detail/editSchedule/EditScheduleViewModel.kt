package com.example.android.medicinecabinet.detail.editSchedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EditScheduleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MedicineRepository
) : ViewModel() {
    val medicineId: Int = savedStateHandle["medicineId"] ?: -1

    private var _medicine = MutableLiveData<Medicine>()
    val medicine: LiveData<Medicine> get() = _medicine

    private var _allDaysThisMeds = repository.allDaysThisMeds
    val allDaysThisMeds: LiveData<List<SelectedTakingDays>> get() = _allDaysThisMeds

    private var _takingTimes = repository.allTimesThisMeds
    val takingTimes: LiveData<List<TakingTime>> get() = _takingTimes

    init {
        repository.setMedsId(medicineId)

        viewModelScope.launch {
            _medicine.value = repository.getOneMedicineById(medicineId)
            // _takingTimes.value = repository.getTimesThisMeds(medicineId)
        }
    }

    private val _navBack = MutableSharedFlow<Unit>()
    var navBack = _navBack.asSharedFlow()

    fun onNavBack(){
        viewModelScope.launch{
            _navBack.emit(Unit)
        }
    }


    fun formatInterval(days: Int?): String {
        return when (days) {
            2 -> "Через день"
            else -> "Каждые $days дней"
        }
    }

    val intervals = (2..100).toList()
    val displayIntervals: List<String> = intervals.map { formatInterval(it) }
}