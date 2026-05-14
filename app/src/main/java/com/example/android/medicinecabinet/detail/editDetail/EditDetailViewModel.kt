package com.example.android.medicinecabinet.detail.editDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.MedicineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EditDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MedicineRepository
) : ViewModel() {

    val medicineId: Int = savedStateHandle["medicineId"] ?: -1

    init {
        loadMedsById(medicineId)
    }

    val nameMeds = MutableLiveData<String>()
    val quantityMeds = MutableLiveData<Int?>()
    val expirationDateMeds = MutableLiveData<String?>()
    val descriptionMeds = MutableLiveData<String?>()

    private var _medicine = MutableLiveData<Medicine>()
    val medicine: LiveData<Medicine> get() = _medicine

    fun loadMedsById(medicineId: Int){
        viewModelScope.launch {
            _medicine.value = repository.getOneMedicineById(medicineId)
            val newMeds = _medicine.value

            nameMeds.value = newMeds?.name
            quantityMeds.value = newMeds?.quantity
            expirationDateMeds.value = newMeds?.expirationDate
            descriptionMeds.value = newMeds?.description
        }
    }

    fun updateMedsName(newName: String){
        nameMeds.value = newName
    }
    fun updateMedsQuantity(newQuantity: Int){
        quantityMeds.value = newQuantity
    }
    fun updateMedsExpDate(newExpDate: String){
        expirationDateMeds.value = newExpDate
    }
    fun updateMedsDescription(newDescription: String){
        descriptionMeds.value = newDescription
    }

    private val _navBack = MutableSharedFlow<Unit>()
    val navBack = _navBack.asSharedFlow()

    fun onNavBack(){
        viewModelScope.launch{
            _navBack.emit(Unit)
        }
    }

}