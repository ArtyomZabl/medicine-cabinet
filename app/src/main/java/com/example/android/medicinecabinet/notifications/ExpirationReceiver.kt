package com.example.android.medicinecabinet.notifications

import android.Manifest
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
import com.example.android.medicinecabinet.utils.Constance
import java.time.LocalDate

class ExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val medName = intent.getStringExtra(Constance.KEY_MEDICINE_NAME) ?: "Лекарство"
            val medId = intent.getIntExtra(Constance.KEY_MEDICINE_ID, -1)
            val expDate = intent.getStringExtra(Constance.KEY_EXP_DATE) ?: ""

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val expiration = LocalDate.parse(expDate)
            val todayDate = LocalDate.now()

            when {
                expiration.isBefore(todayDate) -> {
                    Log.d("EXPIRATION", "Просрочено!")
                    val notification = NotificationCompat.Builder(context, Constance.EXPIRATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Срок годности $medName закончился")
                        .setContentText("Обновите $medName")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSound(alarmSound)
                        .setVibrate(longArrayOf(1000, 1000, 1000))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .build()

                    NotificationManagerCompat.from(context).notify(medId, notification)

                    Alarm.expAlarm(context, medId, medName, expDate)
                }

                expiration.isBefore(todayDate.plusDays(7)) -> {
                    Log.d("EXPIRATION", "Скоро испортится")

                    val notification = NotificationCompat.Builder(context, Constance.EXPIRATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Срок годности $medName скоро истекает")
                        .setContentText("Срок годности $medName истекает $expDate Обновите $medName")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSound(alarmSound)
                        .setVibrate(longArrayOf(1000, 1000, 1000))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .build()

                    NotificationManagerCompat.from(context).notify(medId, notification)

                    Alarm.expAlarm(context, medId, medName, expDate)
                }

                else -> {
                    Log.d("EXPIRATION", "Всё хорошо")
                    Alarm.expAlarm(context, medId, medName, expDate)
                }
            }

        } else {
            Log.d("AlarmDebugger", "Notification permission not granted!")
        }
    }
}