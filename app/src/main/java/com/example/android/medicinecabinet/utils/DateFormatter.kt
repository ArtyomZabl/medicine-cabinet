package com.example.android.medicinecabinet.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    fun format(date: LocalDate, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale("ru"))
        return date.format(formatter)
    }

    fun full(date: LocalDate): String = format(date, "d MMMM yyyy 'г.'")

    fun short(date: LocalDate): String = format(date, "d MMMM '(сегодня)'")

    fun custom(date: LocalDate, pattern: String): String = format(date, pattern)

    fun numericToLong(date: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
        val localDate = LocalDate.parse(date, inputFormatter)

        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru"))
        val formatter = localDate.format(outputFormatter)
        return formatter
    }

}