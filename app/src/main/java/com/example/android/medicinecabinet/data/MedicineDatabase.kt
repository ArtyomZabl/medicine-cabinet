package com.example.android.medicinecabinet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDaysDao
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.data.takingTime.TakingTimeDao

@Database(
    entities = [Medicine::class, TakingTime::class, SelectedTakingDays::class], version = 4, exportSchema = true,
)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun takingTimeDao(): TakingTimeDao
    abstract fun selectedTakingDaysDao(): SelectedTakingDaysDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getDatabase(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val MIGRATION_3_4 = object : Migration(3, 4) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE medicines ADD COLUMN code TEXT")
                    }
                }
                Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "medicines"
                ).addMigrations(MIGRATION_3_4).build().also { INSTANCE = it }
            }
        }
    }
}
