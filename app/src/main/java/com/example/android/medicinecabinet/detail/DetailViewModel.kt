package com.example.android.medicinecabinet.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.utils.WeekDay
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: MedicineRepository): ViewModel() {

    val allTimesThisMeds: LiveData<List<TakingTime>> = repository.allTimesThisMeds

    fun loadTimesAndDaysForMeds(id: Int){
        repository.setMedsId(id)
    }

    fun getSelectedDaysForMedicine(): LiveData<List<WeekDay>>{
        val weekDays = repository.allDaysThisMeds.map { list ->
            list.map { it.weekDay }
        }
        return weekDays
    }

    private var _medicine = MutableLiveData<Medicine>()
    val medicine: LiveData<Medicine> get() = _medicine

    fun loadMedsById(medicineId: Int){
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

    fun delete (medicine: Medicine){
        viewModelScope.launch {
            repository.delete(medicine)
        }
    }

}