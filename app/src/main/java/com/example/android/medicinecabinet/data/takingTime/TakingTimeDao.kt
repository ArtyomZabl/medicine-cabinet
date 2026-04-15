package com.example.android.medicinecabinet.data.takingTime

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TakingTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(time: List<TakingTime>): List<Long>

    @Query("SELECT * FROM taking_time WHERE medicine_id = :id")
    fun getAllTimesThisMeds(id: Int): LiveData<List<TakingTime>>
}
