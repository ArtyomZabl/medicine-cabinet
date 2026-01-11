package com.example.android.medicinecabinet.data.takingTime

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.Relation
import com.example.android.medicinecabinet.data.Medicine
import java.sql.Time

@Entity(tableName = "taking_time")
data class TakingTime(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "medicine_id")
    val medicineId: Int,

    @ColumnInfo(name = "medicine_taking_time")
    val time: String
)


