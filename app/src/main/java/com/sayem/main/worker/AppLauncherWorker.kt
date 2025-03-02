package com.sayem.main.worker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sayem.main.data.ScheduledAppRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import androidx.work.ListenableWorker

@HiltWorker
class AppLauncherWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledAppRepository: ScheduledAppRepository,
    private val packageManager: PackageManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_SCHEDULE_ID = "schedule_id"
        private const val TAG = "AppLauncherWorker"
    }

    override suspend fun doWork(): ListenableWorker.Result {
        val scheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1)
        if (scheduleId == -1L) {
            Log.e(TAG, "Invalid schedule ID")
            return ListenableWorker.Result.failure()
        }

        return try {
            val schedule = scheduledAppRepository.getScheduledAppById(scheduleId).firstOrNull()

            if (schedule == null) {
                Log.e(TAG, "Schedule not found: $scheduleId")
                return ListenableWorker.Result.failure()
            }

            if (schedule.isCancelled) {
                Log.i(TAG, "Schedule was cancelled: $scheduleId")
                return ListenableWorker.Result.failure()
            }

            if (schedule.isExecuted) {
                Log.i(TAG, "Schedule was already executed: $scheduleId")
                return ListenableWorker.Result.failure()
            }

            // Launch the app
            val launchIntent = packageManager.getLaunchIntentForPackage(schedule.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(launchIntent)
                
                // Mark schedule as executed
                scheduledAppRepository.markAsExecuted(scheduleId)
                Log.i(TAG, "Successfully launched app: ${schedule.packageName}")
                ListenableWorker.Result.success()
            } else {
                Log.e(TAG, "Failed to get launch intent for: ${schedule.packageName}")
                ListenableWorker.Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app for schedule $scheduleId", e)
            ListenableWorker.Result.failure()
        }
    }
}
