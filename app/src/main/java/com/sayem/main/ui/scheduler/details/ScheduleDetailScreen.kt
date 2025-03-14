package com.sayem.main.ui.scheduler.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sayem.main.R
import com.sayem.main.ui.shared.CancelledBadge
import com.sayem.main.ui.shared.CustomTimePickerDialog
import com.sayem.main.ui.shared.CustomTopBar
import com.sayem.main.ui.shared.ExecutedBadge
import com.sayem.main.ui.shared.ScheduledBadge
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

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "Schedule Details",
                onBackPressed = onNavigateUp
            )
        },
    ) { paddingValues ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = onNavigateUp) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = "Go back"
//                    )
//                }
//                Text(
//                    text = "Schedule Details",
//                    style = MaterialTheme.typography.headlineMedium
//                )
//                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
//            }

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
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = schedule.appName,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = schedule.packageName,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = {
                                schedule.appIcon?.let { icon ->
                                    Image(
                                        painter = rememberDrawablePainter(drawable = icon),
                                        contentDescription = "App icon for ${schedule.appName}",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            },

                            trailingContent = {
                                when {
                                    schedule.isExecuted -> {
                                        ExecutedBadge()
                                    }

                                    schedule.isCancelled -> {
                                        CancelledBadge()

                                    }

                                    else -> {
                                        ScheduledBadge()
                                    }
                                }
                            }
                        )


                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        MaterialTheme.shapes.medium
                                    ),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.baseline_schedule_24),
                                            contentDescription = "Scheduled time",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.CenterVertically),
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                                        )


                                        Column {
                                            Text(
                                                text = "Scheduled Time",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = schedule.scheduledTime,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }

                                    if (!schedule.isExecuted && !schedule.isCancelled) {
                                        TextButton(
                                            onClick = { showTimePicker = true },
                                        ) {
                                            Text("Change")
                                        }
                                    }
                                }
                            }

                            if (!schedule.isExecuted && !schedule.isCancelled) {

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = onCancel,
                                    colors = ButtonDefaults.buttonColors().copy(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        Image(

                                            painter = painterResource(R.drawable.baseline_delete_24),
                                            contentDescription = "Delete",
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onError)

                                        )
                                        Text("Cancel Schedule")
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }

        if (showTimePicker) {
            CustomTimePickerDialog(
                onUpdateTime = { hour, minute ->
                    onUpdateTime(hour, minute)
                    showTimePicker = false
                },
                onHideDialog = { showTimePicker = false }
            )

        }
    }

}
