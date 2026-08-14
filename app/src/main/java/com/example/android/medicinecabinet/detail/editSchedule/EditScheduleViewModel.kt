package com.example.android.medicinecabinet.detail.editSchedule

import android.util.Log
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
import com.example.android.medicinecabinet.utils.WeekDay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class EditScheduleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MedicineRepository
) : ViewModel() {
    val medicineId: Int = savedStateHandle["medicineId"] ?: -1

    private var _medicine = MutableLiveData<Medicine>()
    val medicine: LiveData<Medicine> get() = _medicine

    private var _takingTimes = repository.allTimesThisMeds
    val takingTimes: LiveData<List<TakingTime>> get() = _takingTimes

    init {
        repository.setMedsId(medicineId)

        viewModelScope.launch {
            _medicine.value = repository.getOneMedicineById(medicineId)
            _allDaysThisMeds.value = repository.getAllDaysThisMeds(medicineId)
            // _takingTimes.value = repository.getTimesThisMeds(medicineId)

            val initialDays = _allDaysThisMeds.value
            _selectedDays.value = initialDays?.map { it.weekDay }?.toMutableList()
            Log.d("EditScheduleViewModel", "initial days: ${_allDaysThisMeds.value}")
            Log.d("EditScheduleViewModel", "Selected days: ${_selectedDays.value}")
        }
    }

    private val _navBack = MutableSharedFlow<Unit>()
    var navBack = _navBack.asSharedFlow()

    fun onNavBack() {
        viewModelScope.launch {
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


    //Week day selector
    private var _allDaysThisMeds = MutableLiveData<List<SelectedTakingDays>>()

    private val _selectedDays = MutableLiveData<MutableList<WeekDay>>()
    val selectedDays: LiveData<MutableList<WeekDay>> get() = _selectedDays

    fun toggleDay(day: WeekDay) {
        val list = _selectedDays.value ?: mutableListOf()

        val newList = if (list.contains(day)) {
            if (list.size > 1) {
                list - day
            } else list
        } else list + day

        _selectedDays.value = newList as MutableList<WeekDay>?

    }
}