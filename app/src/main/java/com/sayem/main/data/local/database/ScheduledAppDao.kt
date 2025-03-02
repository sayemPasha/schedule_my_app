package com.sayem.main.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledAppDao {
    @Query("SELECT * FROM scheduled_apps ORDER BY scheduledTime ASC")
    fun getAllScheduledApps(): Flow<List<ScheduledAppEntity>>

    @Query("SELECT * FROM scheduled_apps WHERE id = :id")
    suspend fun getScheduledAppById(id: Long): ScheduledAppEntity?

    @Query("SELECT * FROM scheduled_apps WHERE scheduledTime > :currentTime AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getUpcomingSchedules(currentTime: Long): Flow<List<ScheduledAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledApp(scheduledApp: ScheduledAppEntity): Long

    @Update
    suspend fun updateScheduledApp(scheduledApp: ScheduledAppEntity)

    @Delete
    suspend fun deleteScheduledApp(scheduledApp: ScheduledAppEntity)

    @Query("UPDATE scheduled_apps SET isCancelled = 1 WHERE id = :id")
    suspend fun cancelSchedule(id: Long)

    @Query("UPDATE scheduled_apps SET isExecuted = 1 WHERE id = :id")
    suspend fun markAsExecuted(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM scheduled_apps WHERE scheduledTime BETWEEN :startTime AND :endTime AND isCancelled = 0)")
    suspend fun hasScheduleConflict(startTime: Long, endTime: Long): Boolean
}
