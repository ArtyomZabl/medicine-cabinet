package com.example.android.medicinecabinet.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDaysDao
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.data.takingTime.TakingTimeDao

class MedicineRepository(
    private val daoMeds: MedicineDao,
    private val daoTime: TakingTimeDao,
    private val daoDays: SelectedTakingDaysDao
) {

    val allMedicines: LiveData<List<Medicine>> = daoMeds.getAllMedicines()

    suspend fun insert(medicine: Medicine): Long {
        return daoMeds.insert(medicine)
    }

    suspend fun delete(medicine: Medicine) {
        daoMeds.delete(medicine)
    }

    suspend fun deleteAll() {
        daoMeds.deleteAll()
    }

    suspend fun getOneMedicineById(id: Int): Medicine? {
        return daoMeds.getOneMedicineById(id)
    }

    suspend fun updateTakingStatus(id: Int, status: Boolean) {
        daoMeds.updateTakingStatus(id, status)
    }



    private val _medicineId = MutableLiveData<Int>()
    val medicineId: LiveData<Int> get() = _medicineId

    fun setMedsId(id: Int) {
        _medicineId.value = id
    }

    val allTimesThisMeds: LiveData<List<TakingTime>> = _medicineId.switchMap { id ->
        daoTime.getAllTimesThisMeds(id)
    }

    fun getTimesThisMeds(medicineId: Int): LiveData<List<TakingTime>> = daoTime.getAllTimesThisMeds(medicineId)

    suspend fun insertAllTimes(time: List<TakingTime>) {
        daoTime.insertAll(time)
    }




    val allDaysThisMeds: LiveData<List<SelectedTakingDays>> = _medicineId.switchMap { id ->
        daoDays.getAllDaysThisMeds(id)
    }

    suspend fun insertAllDays(selectedTakingDay: List<SelectedTakingDays>) {
        daoDays.insertAll(selectedTakingDay)
    }

}