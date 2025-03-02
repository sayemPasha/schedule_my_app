package com.sayem.main.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_apps")
data class ScheduledAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val scheduledTime: Long,
    val isExecuted: Boolean = false,
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
