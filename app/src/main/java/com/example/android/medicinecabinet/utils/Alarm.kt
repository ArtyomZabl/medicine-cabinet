package com.example.android.medicinecabinet.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import java.util.Calendar
import kotlin.jvm.java

object Alarm {

    fun scheduleAlarm(context: Context, medicine: Medicine, times: List<TakingTime>) {
        times.forEach { time ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(Constance.KEY_MEDICINE_NAME, medicine.name)
                putExtra(Constance.KEY_MEDICINE_ID, medicine.medicineId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicine.medicineId + time.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.time.take(2).toInt())
                set(Calendar.MINUTE, time.time.takeLast(2).toInt())
                set(Calendar.SECOND, 0)

                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                } else {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelAlarm(context: Context, medicine: Medicine, times: List<TakingTime>) {
        times.forEach { time ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicine.medicineId + time.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}