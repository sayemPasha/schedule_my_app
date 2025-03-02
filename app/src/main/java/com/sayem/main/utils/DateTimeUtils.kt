package com.sayem.main.utils

import android.text.format.DateFormat
import java.util.Calendar

/**
 * Utility functions for date and time formatting
 */
object DateTimeUtils {
    private const val DATE_TIME_FORMAT = "MMM dd, yyyy hh:mm a"
    private const val TIME_FORMAT = "hh:mm a"

    fun formatDateTime(timestamp: Long): String {
        return DateFormat.format(DATE_TIME_FORMAT, timestamp).toString()
    }

    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return DateFormat.format(TIME_FORMAT, calendar).toString()
    }
}
