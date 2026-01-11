package com.example.android.medicinecabinet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val medicineId: Int = 0,

    @ColumnInfo(name = "medicine_name")
    val name: String,

    @ColumnInfo(name = "medicine_image")
    val image: Int?,

    @ColumnInfo(name = "medicine_quantity")
    val quantity: Int?,

    @ColumnInfo(name = "medicine_dose")
    val dosage: Float?,

    @ColumnInfo(name = "unit")
    val unit: String? = null,

    @ColumnInfo(name = "expiration_date")
    val expirationDate: String?,

    @ColumnInfo(name = "is_taken")
    val isTaken: Boolean = false,

    @ColumnInfo(name = "start_taking_time")
    val startTakingDate: String?,

    @ColumnInfo(name = "end_taking_time")
    val endTakingDate: String?,

    @ColumnInfo(name = "intake_interval_days")
    val intakeIntervalDays: Int?,

    @ColumnInfo(name = "code")
    val code: String?
){
    class MedicineBuilder {
        private var name: String = ""
        private var quantity: Int? = null
        private var image: Int? = null
        private var expirationDate: String? = null
        private var dosage: Float? = null
        private var unit: String? = null
        private var startTakingDate: String? = null
        private var endTakingDate: String? = null
        private var intakeIntervalDays: Int? = null
        private var code: String? = null


        fun name(name: String) = apply { this.name = name }
        fun quantity(quantity: Int?) = apply { this.quantity = quantity }
        fun image(image: Int?) = apply { this.image = quantity }
        fun expirationDate(expirationDate: String?) = apply { this.expirationDate = expirationDate }
        fun dosage(dosage: Float?) = apply { this.dosage = dosage }
        fun unit(unit: String?) = apply { this.unit = unit }
        fun startTakingDate(startTakingDate: String?) = apply { this.startTakingDate = startTakingDate }
        fun endTakingDate(endTakingDate: String?) = apply { this.endTakingDate = endTakingDate }
        fun intakeIntervalDays(intakeIntervalDays: Int?) = apply { this.intakeIntervalDays = intakeIntervalDays }
        fun code(code: String?) = apply { this.code = code }

        fun build() = Medicine(
            name = name,
            quantity = quantity,
            image = image,
            expirationDate = expirationDate,
            dosage = dosage,
            unit = unit,
            startTakingDate = startTakingDate,
            endTakingDate = endTakingDate,
            intakeIntervalDays = intakeIntervalDays,
            code = code
        )
    }
}