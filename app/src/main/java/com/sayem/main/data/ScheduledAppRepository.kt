package com.sayem.main.data

import android.content.pm.PackageManager
import com.sayem.main.data.local.database.ScheduledAppDao
import com.sayem.main.data.local.database.ScheduledAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ScheduledAppRepository @Inject constructor(
    private val scheduledAppDao: ScheduledAppDao,
    private val packageManager: PackageManager
) {
    fun getAllScheduledApps(): Flow<List<ScheduledAppEntity>> =
        scheduledAppDao.getAllScheduledApps()
            .flowOn(Dispatchers.IO)

    fun getUpcomingSchedules(): Flow<List<ScheduledAppEntity>> =
        scheduledAppDao.getUpcomingSchedules(System.currentTimeMillis())
            .flowOn(Dispatchers.IO)

    fun getScheduledAppById(id: Long): Flow<ScheduledAppEntity?> = flow {
        emit(scheduledAppDao.getScheduledAppById(id))
    }.flowOn(Dispatchers.IO)

    suspend fun scheduleApp(packageName: String, appName: String, scheduledTime: Long): Result<Long> {
        return try {
            // Verify app exists
            packageManager.getPackageInfo(packageName, 0)
            
            // Check for time conflicts (5 minute buffer)

            val hasConflict = scheduledAppDao.hasScheduleConflict(
                scheduledTime - BUFFER_TIME,
                scheduledTime + BUFFER_TIME
            )
            
            if (hasConflict) {
                Result.failure(ScheduleConflictException())
            } else {
                val id = scheduledAppDao.insertScheduledApp(
                    ScheduledAppEntity(
                        packageName = packageName,
                        appName = appName,
                        scheduledTime = scheduledTime
                    )
                )
                Result.success(id)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Result.failure(Exception("App not found: $packageName"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSchedule(id: Long, newScheduledTime: Long): Result<Unit> {
        return try {
            val existingSchedule = scheduledAppDao.getScheduledAppById(id)
            if (existingSchedule == null) {
                Result.failure(Exception("Schedule not found"))
            } else if (existingSchedule.isExecuted) {
                Result.failure(Exception("Cannot update executed schedule"))
            } else if (existingSchedule.isCancelled) {
                Result.failure(Exception("Cannot update cancelled schedule"))
            } else {
                // Check for time conflicts
                val bufferTime = 5 * 60 * 1000L
                val hasConflict = scheduledAppDao.hasScheduleConflict(
                    newScheduledTime - bufferTime,
                    newScheduledTime + bufferTime
                )
                
                if (hasConflict) {
                    Result.failure(ScheduleConflictException())
                } else {
                    scheduledAppDao.updateScheduledApp(
                        existingSchedule.copy(scheduledTime = newScheduledTime)
                    )
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelSchedule(id: Long): Result<Unit> {
        return try {
            val schedule = scheduledAppDao.getScheduledAppById(id)
            if (schedule == null) {
                Result.failure(Exception("Schedule not found"))
            } else if (schedule.isExecuted) {
                Result.failure(Exception("Cannot cancel executed schedule"))
            } else {
                scheduledAppDao.cancelSchedule(id)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsExecuted(id: Long) {
        scheduledAppDao.markAsExecuted(id)
    }

    companion object{
        val BUFFER_TIME = 5 * 60 * 1000L
    }
}


class ScheduleConflictException(
    val messageVerbose: String = "Schedule conflict: Another app is scheduled within 5 minutes",
    val permitRetry: Boolean = true
) : Exception(messageVerbose)