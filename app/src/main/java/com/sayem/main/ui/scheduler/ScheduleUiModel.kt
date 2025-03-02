package com.sayem.main.ui.scheduler

data class ScheduleUiModel(
    val id: Long,
    val packageName: String,
    val scheduledTime: String,
    val isExecuted: Boolean,
    val isCancelled: Boolean
)
