package com.example.android.medicinecabinet.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.android.medicinecabinet.data.Medicine
import com.example.android.medicinecabinet.data.takingTime.TakingTime
import com.example.android.medicinecabinet.utils.Constance
import com.example.android.medicinecabinet.notifications.ReminderReceiver
import com.example.android.medicinecabinet.utils.Constance.KEY_SELECTED_DAYS
import com.example.android.medicinecabinet.utils.WeekDay
import java.util.Calendar

object Alarm {

    private fun getReminderRequestCode(medicineId: Int, timeId: Int): Int {
        return "$medicineId-$timeId".hashCode()
    }

    private fun getExpRequestCode(medicineId: Int): Int {
        return "$medicineId".hashCode()
    }

    private fun getDaysUntilNextDay(currentDayValue: Int, selectedDays: List<WeekDay>): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, currentDayValue)

        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_WEEK, 1)
            val nextDayValue = calendar.get(Calendar.DAY_OF_WEEK)
            if (selectedDays.any { it.calendarValue == nextDayValue }) {
                return i
            }
        }
        return 1
    }

    fun scheduleAlarm(
        context: Context,
        medicine: Medicine,
        times: List<TakingTime>,
        interval: Int?,
        selectedDays: List<WeekDay>?
    ) {
        times.forEach { time ->
            singleScheduleAlarm(context, medicine.medicineId, medicine.name, time, interval, selectedDays)
        }
    }

    fun singleScheduleAlarm(
        context: Context,
        medicineId: Int,
        medicineName: String,
        time: TakingTime,
        interval: Int? = 1,
        selectedDays: List<WeekDay>? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(Constance.KEY_MEDICINE_NAME, medicineName)
            putExtra(Constance.KEY_MEDICINE_ID, medicineId)
            putExtra(Constance.KEY_TIME_ID, time.id)
            putExtra(Constance.KEY_TIME_VALUE, time.time)
            putExtra(Constance.KEY_INTERVAL, interval)
            selectedDays?.let { days ->
                putExtra(KEY_SELECTED_DAYS, ArrayList(days.map { it.name }))
            }
            action = "com.example.medicinecabinet.NOTIFICATION_ALARM_$medicineId"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getReminderRequestCode(medicineId, time.id),
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

            val now = System.currentTimeMillis()

            val daysToAdd = if (!selectedDays.isNullOrEmpty()) {
                val currentDayValue = get(Calendar.DAY_OF_WEEK)
                if (timeInMillis <= now || !selectedDays.any { it.calendarValue == currentDayValue }) {
                    getDaysUntilNextDay(currentDayValue, selectedDays)
                } else {
                    0
                }
            } else {
                val step = interval ?: 1
                if (timeInMillis <= now) step else 0
            }

            if (daysToAdd > 0) {
                add(Calendar.DAY_OF_YEAR, daysToAdd)
            }
        }

        Log.d(
            "AlarmDebugger",
            "Scheduling: $medicineName at ${calendar.time} (Code: ${
                getReminderRequestCode(
                    medicineId,
                    time.id
                )
            })"
        )

        val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            }
        } else {
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        }
    }

    fun expAlarm(
        context: Context, medicineId: Int,
        medicineName: String,
        expDate: String?
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExpirationReceiver::class.java).apply {
            putExtra(Constance.KEY_MEDICINE_NAME, medicineName)
            putExtra(Constance.KEY_MEDICINE_ID, medicineId)
            putExtra(Constance.KEY_EXP_DATE, expDate)
            action =
                "com.example.medicinecabinet.CHECK_EXPIRATION_${medicineId}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getExpRequestCode(medicineId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val hour = 12
            val minute = 44


            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d(
            "AlarmDebugger",
            "Scheduling: $medicineName at ${calendar.time} (Code: ${
                getExpRequestCode(
                    medicineId
                )
            })"
        )

        val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            }
        } else {
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        }
    }

    //TODO Finish function canceling
    fun cancelScheduleAlarm(context: Context, medicine: Medicine, times: List<TakingTime>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        times.forEach { time ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.medicinecabinet.NOTIFICATION_ALARM_${medicine.medicineId}"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getReminderRequestCode(medicine.medicineId, time.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent?.cancel()
        }
    }

    fun cancelExpAlarm(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action =
                "com.example.medicinecabinet.CHECK_EXPIRATION_${medicine.medicineId}"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getExpRequestCode(medicine.medicineId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent?.cancel()
    }
}

