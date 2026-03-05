package com.example.android.medicinecabinet.homeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.medicineLog.MedicineLog
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeScreenViewModel(private val repository: MedicineRepository) : ViewModel() {

    val allTakingMedicines: LiveData<List<Medicine>> = repository.allMedicines

    fun getTimesThisMeds(medicineId: Int): LiveData<List<TakingTime>> {
        return repository.getTimesThisMeds(medicineId)
    }

    fun getAllTimes(id: Int) {
        repository.setMedsId(id)
    }

    private var _onNavigateDetail = MutableLiveData<Int?>()
    val onNavigateDetail: LiveData<Int?>
        get() = _onNavigateDetail

    fun onNavigateDetail(id: Int) {
        _onNavigateDetail.value = id
    }

    fun onNavigateDetailDone() {
        _onNavigateDetail.value = null
    }


    private val _logParams = MutableLiveData<Pair<Int, String>>()

    fun getThisMedsLogByDate(medicineId: Int, date: String) {
        _logParams.value = Pair(medicineId, date)
    }

    val medsLogByDate: LiveData<List<MedicineLog>> = _logParams.switchMap { params ->
        repository.getMedsLogByDate(params.first, params.second)
    }

    private val _currentDate = MutableLiveData<String>()

    fun updateLogsDate(date: String) {
        _currentDate.value = date
    }

    val allMedsLogByDate: LiveData<List<MedicineLog>> = _currentDate.switchMap { date ->
        repository.getAllLogsByDate(date)
    }


    fun updateIsTakenState(logId: Int, isTaken: Boolean) {
        viewModelScope.launch {
            repository.updateIsTakenState(logId, isTaken)
        }
    }

    fun insertNewData(medicineLog: MedicineLog) {
        viewModelScope.launch {
            repository.insert(medicineLog)
        }
    }
}