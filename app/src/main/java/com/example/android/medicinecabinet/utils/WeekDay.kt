package com.example.android.medicinecabinet.utils

import android.icu.util.Calendar
import java.time.DayOfWeek

enum class WeekDay(val calendarValue: Int) {
    MON(Calendar.MONDAY),
    TUE(Calendar.TUESDAY),
    WED(Calendar.WEDNESDAY),
    THU(Calendar.THURSDAY),
    FRI(Calendar.FRIDAY),
    SAT(Calendar.SATURDAY),
    SUN(Calendar.SUNDAY);

    companion object {
        fun from(dayOfWeek: DayOfWeek): WeekDay {
            return when (dayOfWeek) {
                DayOfWeek.MONDAY -> MON
                DayOfWeek.TUESDAY -> TUE
                DayOfWeek.WEDNESDAY -> WED
                DayOfWeek.THURSDAY -> THU
                DayOfWeek.FRIDAY -> FRI
                DayOfWeek.SATURDAY -> SAT
                DayOfWeek.SUNDAY -> SUN
            }
        }

        fun fromCalendar(calendarInt: Int): WeekDay{
            return entries.find { it.calendarValue == calendarInt } ?: throw IllegalArgumentException("Invalid calendar value: $calendarInt")
        }

    }


}