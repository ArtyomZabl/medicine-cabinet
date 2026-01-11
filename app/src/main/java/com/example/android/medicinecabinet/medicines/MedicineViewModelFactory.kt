package com.example.android.medicinecabinet.medicines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.medicinecabinet.data.MedicineRepository

class MedicinesViewModelFactory(
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicinesViewModel::class.java)) {

            return MedicinesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}