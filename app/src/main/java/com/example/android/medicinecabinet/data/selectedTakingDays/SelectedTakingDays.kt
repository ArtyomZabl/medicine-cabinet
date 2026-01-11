package com.example.android.medicinecabinet.data.selectedTakingDays

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.medicinecabinet.utils.WeekDay

@Entity(tableName = "selected_taking_days")
data class SelectedTakingDays(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "medicine_id")
    val medicineId: Int,

    @ColumnInfo(name = "week_day")
    val weekDay: WeekDay
)
