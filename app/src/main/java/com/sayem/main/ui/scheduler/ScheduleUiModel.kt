package com.sayem.main.ui.scheduler

import android.graphics.drawable.Drawable

data class ScheduleUiModel(
    val id: Long,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val scheduledTime: String,
    val isExecuted: Boolean,
    val isCancelled: Boolean
)
