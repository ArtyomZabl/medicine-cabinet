package com.example.android.medicinecabinet.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.utils.WeekDay
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MedicineRepository
) : ViewModel() {

    val medicineId: Int = savedStateHandle["medicineId"] ?: -1

    val allTimesThisMeds: LiveData<List<TakingTime>> = repository.allTimesThisMeds

    fun loadTimesAndDaysForMeds(id: Int) {
        repository.setMedsId(id)
    }

    private val _allDaysThisMeds = MutableLiveData<List<SelectedTakingDays>>()
    private val _selectedDays = MutableLiveData<List<WeekDay>>(emptyList())
    val selectedDays: LiveData<List<WeekDay>> get() = _selectedDays

    fun getSelectedDaysForMedicine(): LiveData<List<WeekDay>> {

        return selectedDays

    }

    init {
        repository.setMedsId(medicineId)

        viewModelScope.launch {
            _allDaysThisMeds.value = repository.getAllDaysThisMeds(medicineId)
            _selectedDays.value = _allDaysThisMeds.value?.map { it.weekDay }?.toMutableList()
        }

    }

    private var _medicine = MutableLiveData<Medicine>()
    val medicine: LiveData<Medicine> get() = _medicine

    fun loadMedsById(medicineId: Int) {
        viewModelScope.launch {
            _medicine.value = repository.getOneMedicineById(medicineId)
        }
    }

    private var _onClickBack = MutableSharedFlow<Unit>()
    val onClickBack = _onClickBack.asSharedFlow()

    fun onNavigateBack() {
        viewModelScope.launch {
            _onClickBack.emit(Unit)
        }
    }

    private var _onClickDelete = MutableSharedFlow<Unit>()
    val onClickDelete = _onClickDelete.asSharedFlow()

    fun onDeleteClicked() {
        viewModelScope.launch {
            _onClickDelete.emit(Unit)
        }
    }

    fun delete(medicine: Medicine) {
        viewModelScope.launch {
            repository.delete(medicine)
        }
    }


    private var _navigateToEdit = MutableSharedFlow<Unit>()
    val navigateToEdit = _navigateToEdit.asSharedFlow()

    fun onNavigateToEdit() {
        viewModelScope.launch {
            _navigateToEdit.emit(Unit)
        }
    }

    private var _navigateToSchedule = MutableSharedFlow<Unit>()
    val navigateToSchedule = _navigateToSchedule.asSharedFlow()

    fun onNavigateToSchedule() {
        viewModelScope.launch {
            _navigateToSchedule.emit(Unit)
        }
    }
}