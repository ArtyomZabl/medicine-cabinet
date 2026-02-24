package com.example.android.medicinecabinet.homeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.takingTime.TakingTime

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


}