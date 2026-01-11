package com.example.android.medicinecabinet.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("DELETE FROM medicines")
    suspend fun deleteAll()

    @Update
    suspend fun update(medicine: Medicine)

    @Query("SELECT * FROM medicines")
    fun getAll(): LiveData<List<Medicine>>

    @Query("SELECT * FROM medicines")
    fun getAllMedicines(): LiveData<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE medicineId = :id")
    suspend fun getOneMedicineById(id: Int): Medicine?

    @Query("UPDATE medicines SET is_taken = :status WHERE medicineId = :id")
    suspend fun updateTakingStatus(id: Int, status: Boolean)
}