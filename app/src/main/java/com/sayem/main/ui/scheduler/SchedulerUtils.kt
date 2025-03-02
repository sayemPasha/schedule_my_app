package com.sayem.main.ui.scheduler

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        .format(Date(timestamp))
}

internal fun getFormattedTime(hour: Int, minute: Int): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
        Date().apply {
            hours = hour
            minutes = minute
        }
    )
}
