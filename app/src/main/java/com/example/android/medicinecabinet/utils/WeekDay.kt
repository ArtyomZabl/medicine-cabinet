package com.example.android.medicinecabinet.utils

import java.time.DayOfWeek

enum class WeekDay() {
    MON, TUE, WED, THU, FRI, SAT, SUN;

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
    }
}