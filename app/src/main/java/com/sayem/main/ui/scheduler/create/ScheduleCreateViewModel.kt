package com.sayem.main.ui.scheduler.create

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.sayem.main.data.ScheduledAppRepository
import com.sayem.main.worker.ScheduleManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayem.main.data.ScheduleConflictException
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
                .map { 
                    AppInfo(
                        packageName = it.packageName,
                        label = it.loadLabel(packageManager).toString(),
                        icon = it.loadIcon(packageManager)
                    )
                }
                .sortedBy { it.label }

            _uiState.update { ScheduleCreateUiState.Success(installedApps) }
        }
    }

    fun scheduleApp(packageName: String, hour: Int, minute: Int, onScheduleCreationSuccess: () -> Unit) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time has passed
                }
            }

            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = appInfo.loadLabel(packageManager).toString()
            repository.scheduleApp(packageName, appName, calendar.timeInMillis)
                .onSuccess { scheduleId ->
                    scheduleManager.scheduleApp(scheduleId, calendar.timeInMillis)
                    onScheduleCreationSuccess()
                }
                .onFailure { e->
                    when(e) {
                        is PackageManager.NameNotFoundException -> {
                            // App not found
                        }
                        is ScheduleConflictException -> {
                            (e as? ScheduleConflictException)?.let {
                                showErrorDialog(it.messageVerbose)
                            }
                        }
                        else -> {
                            // Other error
                        }
                    }

                }
        }
    }

    fun dismissDialog() {
        _uiState.update {
            (it as? ScheduleCreateUiState.Success)?.copy(
                showAlertDialog = false
            ) ?: it
        }
    }

    private fun showErrorDialog(message: String) {
        _uiState.update{
            (it as? ScheduleCreateUiState.Success)?.copy(
                showAlertDialog = true,
                dialogContent = message
            ) ?: it
        }
    }
}

sealed interface ScheduleCreateUiState {
    object Loading : ScheduleCreateUiState
    data class Success(
        val apps: List<AppInfo>,
        val showAlertDialog: Boolean = false,
        val dialogContent: String = ""
    ) : ScheduleCreateUiState
}

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable
)
