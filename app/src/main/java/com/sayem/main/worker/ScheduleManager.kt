package com.sayem.main.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val TAG = "ScheduleManager"
    }

    fun scheduleApp(scheduleId: Long, scheduledTime: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = scheduledTime - currentTime

        // Only schedule if the time hasn't passed
        if (delay <= 0) {
            Log.w(TAG, "Attempted to schedule app for past time: $scheduledTime")
            return
        }

        try {
            val inputData = Data.Builder()
                .putLong(AppLauncherWorker.KEY_SCHEDULE_ID, scheduleId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AppLauncherWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            // Use unique work to ensure we can cancel it later
            workManager.enqueueUniqueWork(
                getWorkName(scheduleId),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.i(TAG, "Successfully scheduled app with ID: $scheduleId for time: $scheduledTime")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule app with ID: $scheduleId", e)
        }
    }

    fun cancelSchedule(scheduleId: Long) {
        try {
            workManager.cancelUniqueWork(getWorkName(scheduleId))
            Log.i(TAG, "Successfully cancelled schedule with ID: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel schedule with ID: $scheduleId", e)
        }
    }

    private fun getWorkName(scheduleId: Long) = "schedule_$scheduleId"
}
