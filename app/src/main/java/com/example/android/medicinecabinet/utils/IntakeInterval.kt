package com.example.android.medicinecabinet.utils

import com.example.android.medicinecabinet.R

enum class IntakeInterval(val stringRes: Int) {
    AS_NEEDED(R.string.interval_as_needed),
    EVERY_DAY(R.string.interval_every_day),
    SPECIFIC_DAYS(R.string.interval_specific_days),
    EVERY_X_DAYS(R.string.interval_every_x_days)
}