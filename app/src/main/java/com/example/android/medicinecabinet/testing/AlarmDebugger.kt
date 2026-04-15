package com.example.android.medicinecabinet.testing

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.utils.ReminderReceiver

object AlarmDebugger {
    private const val TAG = "AlarmDebugger"

    fun debugAlarmStatus(context: Context, medicine: Medicine, times: List<TakingTime>) {
        Log.d(TAG, "=== DEBUG START for ${medicine.name} ===")

        // Проверка разрешений
        val notificationEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $notificationEnabled")

        // Проверка канала
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(Constance.MEDICINE_CHANNEL_ID)
            if (channel != null) {
                Log.d(TAG, "Channel found: ${channel.name}")
                Log.d(TAG, "Channel Importance: ${channel.importance}") // 4 = HIGH
                Log.d(TAG, "Channel Sound: ${channel.sound}")
                Log.d(TAG, "Channel Vibration: ${channel.shouldVibrate()}")
                Log.d(TAG, "Channel Lockscreen: ${channel.lockscreenVisibility}")
            } else {
                Log.e(TAG, "CHANNEL NOT FOUND! ID: ${Constance.MEDICINE_CHANNEL_ID}")
            }

        }

        // Проверка ExactAlarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d(TAG, "Can schedule exact alarms: ${alarmManager.canScheduleExactAlarms()}")
        }

        // Проверка существования алармов
        times.forEach { time ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.medicinecabinet.NOTIFICATION_ALARM_${medicine.medicineId}"
            }
            
            val requestCode = "${medicine.medicineId}-${time.id}".hashCode()
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            val isScheduled = pendingIntent != null
            Log.d(TAG, "Alarm for ${medicine.name} at ${time.time} (ReqCode: $requestCode) is scheduled: $isScheduled")
        }
        Log.d(TAG, "=== DEBUG END ===")
    }
}