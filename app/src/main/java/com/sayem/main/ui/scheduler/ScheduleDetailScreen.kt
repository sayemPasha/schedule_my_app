package com.sayem.main.ui.scheduler

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Calendar

@Composable
fun ScheduleDetailRoute(
    scheduleId: Long,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.getSchedule(scheduleId).collectAsStateWithLifecycle(initialValue = null)
    ScheduleDetailScreen(
        schedule = uiState,
        onNavigateUp = onNavigateUp,
        onUpdateTime = { hour, minute -> viewModel.updateScheduleTime(scheduleId, hour, minute) },
        onCancel = {
            viewModel.cancelSchedule(scheduleId)
            onNavigateUp()
        },
        modifier = modifier
    )
}

@Composable
fun ScheduleDetailScreen(
    schedule: ScheduleUiModel?,
    onNavigateUp: () -> Unit,
    onUpdateTime: (Int, Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back"
                )
            }
            Text(
                text = "Schedule Details",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }

        if (schedule == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = schedule.packageName,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Scheduled for: ${schedule.scheduledTime}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = when {
                        schedule.isExecuted -> "Status: Executed"
                        schedule.isCancelled -> "Status: Cancelled"
                        else -> "Status: Scheduled"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!schedule.isExecuted && !schedule.isCancelled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Time")
                    }

                    Button(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Schedule")
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onUpdateTime(hour, minute)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }
}
