package com.sayem.main.ui.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.utils.DateTimeUtils
import com.sayem.main.worker.ScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    private val repository: ScheduledAppRepository,
    private val scheduleManager: ScheduleManager
) : ViewModel() {

    fun getSchedule(id: Long): StateFlow<ScheduleUiModel?> {
        return repository.getScheduledAppById(id)
            .map { schedule ->
                schedule?.let { entity ->
                    ScheduleUiModel(
                        id = entity.id,
                        packageName = entity.packageName,
                        scheduledTime = DateTimeUtils.formatDateTime(entity.scheduledTime),
                        isExecuted = entity.isExecuted,
                        isCancelled = entity.isCancelled
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun updateScheduleTime(id: Long, hour: Int, minute: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time has passed
                }
            }

            repository.updateSchedule(id, calendar.timeInMillis)
                .onSuccess { scheduleManager.scheduleApp(id, calendar.timeInMillis) }
        }
    }

    fun cancelSchedule(id: Long) {
        viewModelScope.launch {
            repository.cancelSchedule(id)
                .onSuccess { scheduleManager.cancelSchedule(id) }
        }
    }
}
