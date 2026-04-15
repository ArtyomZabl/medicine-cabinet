package com.example.android.medicinecabinet.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import java.util.Calendar

object Alarm {

    private fun getRequestCode(medicineId: Int, timeId: Int): Int {
        return "$medicineId-$timeId".hashCode()
    }

    fun scheduleAlarm(context: Context, medicine: Medicine, times: List<TakingTime>) {
        times.forEach { time ->
            singleScheduleAlarm(context, medicine.medicineId, medicine.name, time)
        }
    }

    fun singleScheduleAlarm(
        context: Context,
        medicineId: Int,
        medicineName: String,
        time: TakingTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(Constance.KEY_MEDICINE_NAME, medicineName)
            putExtra(Constance.KEY_MEDICINE_ID, medicineId)
            putExtra(Constance.KEY_TIME_ID, time.id)
            putExtra(Constance.KEY_TIME_VALUE, time.time)
            action = "com.example.medicinecabinet.NOTIFICATION_ALARM_$medicineId"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(medicineId, time.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val hour = time.time.split(":")[0].toInt()
            val minute = time.time.split(":")[1].toInt()

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("AlarmDebugger", "Scheduling: $medicineName at ${calendar.time} (Code: ${getRequestCode(medicineId, time.id)})")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, medicine: Medicine, times: List<TakingTime>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        times.forEach { time ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.medicinecabinet.NOTIFICATION_ALARM_${medicine.medicineId}"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(medicine.medicineId, time.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent?.cancel()
        }
    }
}