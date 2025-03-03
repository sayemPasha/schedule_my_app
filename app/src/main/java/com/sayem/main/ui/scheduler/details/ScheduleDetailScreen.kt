package com.sayem.main.ui.scheduler.details

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load schedule when screen is created
    androidx.compose.runtime.LaunchedEffect(scheduleId) {
        viewModel.loadSchedule(scheduleId)
    }
    
    ScheduleDetailScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onUpdateTime = { hour, minute -> viewModel.updateScheduleTime(scheduleId, hour, minute) },
        onCancel = {
            viewModel.cancelSchedule(scheduleId)
            onNavigateUp()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    uiState: ScheduleDetailUiState,
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

        when (uiState) {
            is ScheduleDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is ScheduleDetailUiState.NotFound -> {
                Text(
                    text = "Schedule not found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is ScheduleDetailUiState.Success -> {
                val schedule = uiState.schedule
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
    }

    if (showTimePicker) {
        val currentTime = Calendar.getInstance()
//        TimePickerDialog(
//            context,
//            { _, hour, minute ->
//                onUpdateTime(hour, minute)
//                showTimePicker = false
//            },
//            calendar.get(Calendar.HOUR_OF_DAY),
//            calendar.get(Calendar.MINUTE),
//            false
//        ).show()

        Dialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = {}
        ){
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            ){
                Column(modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 40.dp)
                    .fillMaxWidth()){
                    val timePickerState = rememberTimePickerState(
                        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
                        initialMinute = currentTime.get(Calendar.MINUTE),
                        is24Hour = false,
                    )

                    TimePicker(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        state = timePickerState,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton (onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            onUpdateTime(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) {
                            Text("Confirm selection")
                        }
                    }
                }
            }
        }

    }
}
