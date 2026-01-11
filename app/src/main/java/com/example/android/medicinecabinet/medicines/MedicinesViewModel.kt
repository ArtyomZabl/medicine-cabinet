package com.example.android.medicinecabinet.medicines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MedicinesViewModel(private val repository: MedicineRepository) : ViewModel() {

    // Navigate to add fragment button
    private var _onClickAddButton = MutableLiveData<Boolean?>()
    val onClickAddButton: LiveData<Boolean?>
        get() = _onClickAddButton

    fun onNavigateAddDone() {
        _onClickAddButton.value = null
    }

    fun navigateToAddMedicine() {
        _onClickAddButton.value = true
    }


    // Delete data base
    private var _onClickDeleteAllButton = MutableLiveData<Boolean?>()
    val onClickDeleteAllButton: LiveData<Boolean?>
        get() = _onClickDeleteAllButton

    fun clearDatabase() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    val allMedicines: LiveData<List<Medicine>> = repository.allMedicines


    private var _onClickDetail = MutableLiveData<Int?>()
    val onClickDetail: LiveData<Int?>
        get() = _onClickDetail

    fun onNavigateDetail(id: Int){
        _onClickDetail.value = id
    }

    fun onNavigateDetailDone(){
        _onClickDetail.value = null
    }
}