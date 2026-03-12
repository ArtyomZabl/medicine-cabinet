package com.example.android.medicinecabinet.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.android.medicinecabinet.R

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val name = intent.getStringExtra(Constance.KEY_MEDICINE_NAME) ?: "Лекарство"

            val notification = NotificationCompat.Builder(context, Constance.MEDICINE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Время принимать $name")
                .setContentText("Примите $name")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

            NotificationManagerCompat.from(context).notify(
                intent.getIntExtra(Constance.KEY_MEDICINE_ID, 0),
                notification
            )
        }
    }
}