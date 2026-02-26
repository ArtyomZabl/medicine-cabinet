package com.example.android.medicinecabinet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.medicinecabinet.data.medicineLog.MedicineLog
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDays
import com.example.android.medicinecabinet.data.selectedTakingDays.SelectedTakingDaysDao
import com.example.android.medicinecabinet.data.medicineLog.MedicineLogDao
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.data.takingTime.TakingTimeDao

@Database(
    entities = [Medicine::class, TakingTime::class, SelectedTakingDays::class, MedicineLog::class],
    version = 7,
    exportSchema = true,
)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun takingTimeDao(): TakingTimeDao
    abstract fun selectedTakingDaysDao(): SelectedTakingDaysDao
    abstract fun medicineLogDao(): MedicineLogDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getDatabase(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val MIGRATION_3_4 = object : Migration(3, 4) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE medicines ADD COLUMN code TEXT")
                    }
                }
                val MIGRATION_4_5 = object : Migration(4, 5) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE medicines ADD COLUMN description TEXT")
                    }
                }
                val MIGRATION_5_6 = object : Migration(5, 6) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE medicines ADD COLUMN medicine_image_path TEXT")
                    }
                }
                val MIGRATION_6_7 = object : Migration(6, 7) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `medicine_log` (" +
                                    "`logId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "`taking_time_id` INTEGER NOT NULL, " +
                                    "`medicine_id` INTEGER NOT NULL, " +
                                    "`date_taken` TEXT NOT NULL, " +
                                    "`is_taken` INTEGER NOT NULL," +
                                    "FOREIGN KEY ('taking_time_id') REFERENCES 'taking_time'('id') ON UPDATE NO ACTION ON DELETE CASCADE )"
                                        .trimIndent()
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS " +
                                    "`index_medicine_log_taking_time_id` ON `medicine_log` (`taking_time_id`)"
                        )
                    }
                }
                Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "medicines"
                ).addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
