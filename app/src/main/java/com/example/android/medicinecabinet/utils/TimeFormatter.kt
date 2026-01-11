package com.example.android.medicinecabinet.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormatter {

    fun format(date: LocalDateTime, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale("ru"))
        return date.format(formatter)
    }

    fun full(time: LocalDateTime): String = format(time, "HH:mm:ss")

    fun short(time: LocalDateTime): String = format(time, "HH:mm")

    fun custom(time: LocalDateTime, pattern: String): String = format(time, pattern)


}