package com.example.android.medicinecabinet.data.medicineLog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.medicinecabinet.data.takingTime.TakingTime

@Entity(
    tableName = "medicine_log",
    foreignKeys = [
        ForeignKey(
            entity = TakingTime::class,
            parentColumns = ["id"],
            childColumns = ["taking_time_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices =[Index("taking_time_id")]
)
data class MedicineLog(
    @PrimaryKey(autoGenerate = true)
    val logId: Int = 0,

    @ColumnInfo(name = "date_taken")
    val dateTaken: String,

    @ColumnInfo(name = "medicine_id")
    val medicineId: Int,

    @ColumnInfo(name = "taking_time_id")
    val takingTimeId: Int,

    @ColumnInfo(name = "is_taken")
    val isTaken: Boolean
)