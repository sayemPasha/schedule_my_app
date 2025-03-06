package com.sayem.main.ui.scheduler.details

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.ui.scheduler.ScheduleUiModel
import com.sayem.main.utils.DateTimeUtils
import com.sayem.main.worker.ScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed interface ScheduleDetailUiState {
    object Loading : ScheduleDetailUiState
    data class Success(val schedule: ScheduleUiModel) : ScheduleDetailUiState
    object NotFound : ScheduleDetailUiState
}

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    private val repository: ScheduledAppRepository,
    private val scheduleManager: ScheduleManager,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleDetailUiState>(ScheduleDetailUiState.Loading)
    val uiState: StateFlow<ScheduleDetailUiState> = _uiState.asStateFlow()

    fun loadSchedule(id: Long) {
        viewModelScope.launch {
            repository.getScheduledAppById(id)
                .map { schedule ->
                    schedule?.let { entity ->
                        ScheduleDetailUiState.Success(
                            try {
                                val appInfo = packageManager.getApplicationInfo(entity.packageName, 0)
                                ScheduleUiModel(
                                    id = entity.id,
                                    packageName = entity.packageName,
                                    appName = entity.appName,
                                    appIcon = appInfo.loadIcon(packageManager),
                                    scheduledTime = DateTimeUtils.formatDateTime(entity.scheduledTime),
                                    isExecuted = entity.isExecuted,
                                    isCancelled = entity.isCancelled
                                )
                            } catch (e: PackageManager.NameNotFoundException) {
                                ScheduleUiModel(
                                    id = entity.id,
                                    packageName = entity.packageName,
                                    appName = entity.appName,
                                    appIcon = null,
                                    scheduledTime = DateTimeUtils.formatDateTime(entity.scheduledTime),
                                    isExecuted = entity.isExecuted,
                                    isCancelled = entity.isCancelled
                                )
                            }
                        )
                    } ?: ScheduleDetailUiState.NotFound
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ScheduleDetailUiState.Loading
                )
                .collect { state ->
                    _uiState.update { state }
                }
        }
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
                .onSuccess {
                    scheduleManager.scheduleApp(id, calendar.timeInMillis)
                    loadSchedule(id)
                }
        }
    }

    fun cancelSchedule(id: Long) {
        viewModelScope.launch {
            repository.cancelSchedule(id)
                .onSuccess { scheduleManager.cancelSchedule(id) }
        }
    }
}
