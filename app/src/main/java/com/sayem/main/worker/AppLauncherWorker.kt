package com.sayem.main.worker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.data.local.database.ScheduledAppEntity
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class AppLauncherWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledAppRepository: ScheduledAppRepository,
    private val packageManager: PackageManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_SCHEDULE_ID = "schedule_id"
    }

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1)
        if (scheduleId == -1L) {
            return Result.failure()
        }

        return try {
            val schedule = scheduledAppRepository.getScheduledAppById(scheduleId).firstOrNull()

            if (schedule == null || schedule.isCancelled) {
                return Result.failure()
            }

            // Launch the app
            val launchIntent = packageManager.getLaunchIntentForPackage(schedule.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(launchIntent)
                
                // Mark schedule as executed
                scheduledAppRepository.markAsExecuted(scheduleId)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
