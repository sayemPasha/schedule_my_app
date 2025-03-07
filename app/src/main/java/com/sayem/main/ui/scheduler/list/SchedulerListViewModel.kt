package com.sayem.main.ui.scheduler.list

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.ui.scheduler.ScheduleUiModel
import com.sayem.main.worker.ScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ScheduleFilter {
    All,
    Scheduled,
    Executed,
    Cancelled
}

@HiltViewModel
class SchedulerListViewModel @Inject constructor(
    private val repository: ScheduledAppRepository,
    private val scheduleManager: ScheduleManager,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(ScheduleFilter.All)

    val uiState: StateFlow<SchedulerListUiState> = combine(
        repository.getAllScheduledApps(),
        _selectedFilter
    ) { schedules, filter ->
        val mappedSchedules = schedules.map { schedule ->
            try {
                val appInfo = packageManager.getApplicationInfo(schedule.packageName, 0)
                ScheduleUiModel(
                    id = schedule.id,
                    packageName = schedule.packageName,
                    appName = schedule.appName,
                    appIcon = appInfo.loadIcon(packageManager),
                    scheduledTime = formatDateTime(schedule.scheduledTime),
                    isExecuted = schedule.isExecuted,
                    isCancelled = schedule.isCancelled
                )
            } catch (e: PackageManager.NameNotFoundException) {
                ScheduleUiModel(
                    id = schedule.id,
                    packageName = schedule.packageName,
                    appName = schedule.appName,
                    appIcon = null,
                    scheduledTime = formatDateTime(schedule.scheduledTime),
                    isExecuted = schedule.isExecuted,
                    isCancelled = schedule.isCancelled
                )
            }
        }

        val filteredSchedules = when (filter) {
            ScheduleFilter.All -> mappedSchedules
            ScheduleFilter.Scheduled -> mappedSchedules.filter { !it.isExecuted && !it.isCancelled }
            ScheduleFilter.Executed -> mappedSchedules.filter { it.isExecuted }
            ScheduleFilter.Cancelled -> mappedSchedules.filter { it.isCancelled }
        }

        SchedulerListUiState.Success(
            schedules = filteredSchedules,
            selectedFilter = filter
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SchedulerListUiState.Loading
    )

    fun setFilter(filter: ScheduleFilter) {
        _selectedFilter.value = filter
    }

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
    data class Success(
        val schedules: List<ScheduleUiModel>,
        val selectedFilter: ScheduleFilter = ScheduleFilter.All
    ) : SchedulerListUiState
}
