package com.example.android.medicinecabinet.homeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val repository: MedicineRepository) : ViewModel() {

    val allTakingMedicines: LiveData<List<Medicine>> = repository.allMedicines

    private var _onNavigateDetail = MutableLiveData<Int?>()
    val onNavigateDetail: LiveData<Int?>
        get() = _onNavigateDetail

    fun onNavigateDetail(id: Int) {
        _onNavigateDetail.value = id
    }
    fun onNavigateDetailDone(){
        _onNavigateDetail.value = null
    }



}