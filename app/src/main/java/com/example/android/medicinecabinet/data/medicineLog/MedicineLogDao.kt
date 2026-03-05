package com.example.android.medicinecabinet.data.medicineLog

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MedicineLogDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(medicineLog: MedicineLog)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(medicineLog: List<MedicineLog>)

    @Query("SELECT * FROM medicine_log WHERE date_taken = :date")
    fun getAllLogsThisDate(date: String): LiveData<List<MedicineLog>>

    @Query("SELECT * FROM medicine_log WHERE medicine_id = :medicineId AND date_taken = :date")
    fun getMedsLogByDate(medicineId: Int, date: String): LiveData<List<MedicineLog>>

    @Query("UPDATE medicine_log SET is_taken = :isTaken WHERE logId = :logId")
    suspend fun updateIsTakenState(logId: Int, isTaken: Boolean)
}