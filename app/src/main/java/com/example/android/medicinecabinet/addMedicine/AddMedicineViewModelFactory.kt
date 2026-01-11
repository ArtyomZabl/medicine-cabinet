package com.example.android.medicinecabinet.addMedicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.medicinecabinet.data.MedicineRepository
import com.example.android.medicinecabinet.data.takingTime.TakingTime

class AddMedicineViewModelFactory(
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddMedicineViewModel::class.java)) {

            return AddMedicineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}