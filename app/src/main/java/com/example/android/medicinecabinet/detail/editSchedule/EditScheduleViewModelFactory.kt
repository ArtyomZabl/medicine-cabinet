package com.example.android.medicinecabinet.detail.editSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.android.medicinecabinet.data.MedicineRepository

class EditScheduleViewModelFactory(
    private val repository: MedicineRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(EditScheduleViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            return EditScheduleViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}