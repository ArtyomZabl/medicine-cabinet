package com.example.android.medicinecabinet.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.android.medicinecabinet.R
import com.example.android.medicinecabinet.data.takingTime.TakingTime

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d(
            "AlarmDebugger",
            "ReminderReceiver received alarm for id: ${
                intent.getIntExtra(
                    Constance.KEY_MEDICINE_ID,
                    0
                )
            }"
        )

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val medName = intent.getStringExtra(Constance.KEY_MEDICINE_NAME) ?: "Лекарство"
            val medId = intent.getIntExtra(Constance.KEY_MEDICINE_ID, -1)
            val timeId = intent.getIntExtra(Constance.KEY_TIME_ID, -1)
            val timeValue = intent.getStringExtra(Constance.KEY_TIME_VALUE) ?: ""

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, Constance.MEDICINE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Время принимать $medName")
                .setContentText("Примите $medName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(alarmSound)
                .setVibrate(longArrayOf(1000, 1000, 1000))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(medId, notification)

            if (medId != -1 && timeId != -1 && timeValue.isNotEmpty()) {
                val nextTime = TakingTime(
                    id = timeId,
                    medicineId = medId,
                    time = timeValue
                )
                Alarm.singleScheduleAlarm(context, medId, medName, nextTime)
            }

        } else {
            Log.d("AlarmDebugger", "Notification permission not granted!")
        }


    }
}