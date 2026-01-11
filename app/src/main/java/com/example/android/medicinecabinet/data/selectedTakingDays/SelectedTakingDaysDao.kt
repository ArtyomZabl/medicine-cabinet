package com.example.android.medicinecabinet.data.selectedTakingDays

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.medicinecabinet.data.takingTime.TakingTime

@Dao
interface SelectedTakingDaysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(selectedTakingDay: SelectedTakingDays)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selectedTakingDay: List<SelectedTakingDays>)

    @Query("SELECT * FROM selected_taking_days WHERE medicine_id = :id")
    fun getAllDaysThisMeds(id: Int): LiveData<List<SelectedTakingDays>>
}