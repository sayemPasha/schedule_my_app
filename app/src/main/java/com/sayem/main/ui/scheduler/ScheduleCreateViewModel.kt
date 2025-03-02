package com.sayem.main.ui.scheduler

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.worker.ScheduleManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleCreateViewModel @Inject constructor(
    private val repository: ScheduledAppRepository,
    private val scheduleManager: ScheduleManager,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleCreateUiState>(ScheduleCreateUiState.Loading)
    val uiState: StateFlow<ScheduleCreateUiState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val installedApps = packageManager.getInstalledApplications(0)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Non-system apps
                .map { AppInfo(it.packageName, it.loadLabel(packageManager).toString()) }
                .sortedBy { it.label }

            _uiState.update { ScheduleCreateUiState.Success(installedApps) }
        }
    }

    fun scheduleApp(packageName: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time has passed
                }
            }

            repository.scheduleApp(packageName, calendar.timeInMillis)
                .onSuccess { scheduleId ->
                    scheduleManager.scheduleApp(scheduleId, calendar.timeInMillis)
                }
        }
    }
}

sealed interface ScheduleCreateUiState {
    object Loading : ScheduleCreateUiState
    data class Success(val apps: List<AppInfo>) : ScheduleCreateUiState
}

data class AppInfo(
    val packageName: String,
    val label: String
)
