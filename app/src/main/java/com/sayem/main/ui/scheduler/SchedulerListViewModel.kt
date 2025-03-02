package com.sayem.main.ui.scheduler

import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.worker.ScheduleManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SchedulerListViewModel @Inject constructor(
    private val repository: ScheduledAppRepository,
    private val scheduleManager: ScheduleManager
) : ViewModel() {

    val uiState: StateFlow<SchedulerListUiState> = repository.getAllScheduledApps()
        .map { schedules ->
            SchedulerListUiState.Success(
                schedules.map { schedule ->
                    ScheduleUiModel(
                        id = schedule.id,
                        packageName = schedule.packageName,
                        scheduledTime = formatDateTime(schedule.scheduledTime),
                        isExecuted = schedule.isExecuted,
                        isCancelled = schedule.isCancelled
                    )
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchedulerListUiState.Loading
        )

    fun cancelSchedule(id: Long) {
        viewModelScope.launch {
            repository.cancelSchedule(id).onSuccess {
                scheduleManager.cancelSchedule(id)
            }
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            .format(Date(timestamp))
    }
}

sealed interface SchedulerListUiState {
    object Loading : SchedulerListUiState
    data class Success(val schedules: List<ScheduleUiModel>) : SchedulerListUiState
}

data class ScheduleUiModel(
    val id: Long,
    val packageName: String,
    val scheduledTime: String,
    val isExecuted: Boolean,
    val isCancelled: Boolean
)
