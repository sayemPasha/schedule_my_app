package com.sayem.main.ui.scheduler.create

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import com.google.accompanist.drawablepainter.rememberDrawablePainter
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sayem.main.R
import com.sayem.main.ui.shared.CustomTimePickerDialog
import com.sayem.main.ui.shared.CustomTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleCreateRoute(
    onScheduleCreated: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScheduleCreateScreen(
        uiState = uiState,
        onScheduleApp = { packageName, hour, minute ->
            viewModel.scheduleApp(packageName, hour, minute)
            onScheduleCreated()
        },
        onNavigateUp = onNavigateUp,
        modifier = modifier
    )
}

@Composable
fun ScheduleCreateScreen(
    uiState: ScheduleCreateUiState,
    onScheduleApp: (String, Int, Int) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CustomTopBar(
                text = "Create Schedule",
                onBackPressed = onNavigateUp
            )
        },
    ) { paddingValues ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search apps") },
                modifier = Modifier.fillMaxWidth()
            )

            when (uiState) {
                ScheduleCreateUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is ScheduleCreateUiState.Success -> {
                    val filteredApps = uiState.apps.filter { app ->
                        app.label.contains(searchQuery, ignoreCase = true) ||
                                app.packageName.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps) { app ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                border = if(selectedApp == app)
                                    BorderStroke(
                                        width = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    ) else null,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedApp = app }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedApp == app,
                                            onClick = { selectedApp = app }
                                        )
                                        Image(
                                            painter = rememberDrawablePainter(drawable = app.icon),
                                            contentDescription = "App icon for ${app.label}",
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(

                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        MaterialTheme.shapes.medium
                    ),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 20.dp),
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



                        Text(
                            text = formatTime(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.titleMedium
                        )

                    }


                    TextButton(
                        onClick = { showTimePicker = true },
                    ) {
                        Text("Select Time")
                    }

                }
            }



//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = formatTime(selectedHour, selectedMinute),
//                    style = MaterialTheme.typography.titleMedium
//                )
//                Button(
//                    onClick = {
//                        TimePickerDialog(
//                            context,
//                            { _, hour, minute ->
//                                selectedHour = hour
//                                selectedMinute = minute
//                            },
//                            selectedHour,
//                            selectedMinute,
//                            false
//                        ).show()
//                    }
//                ) {
//                    Text("Select Time")
//                }
//            }

            Button(
                onClick = {
                    selectedApp?.let { app ->
                        onScheduleApp(app.packageName, selectedHour, selectedMinute)
                    }
                },
                enabled = selectedApp != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule App")
            }
        }

        if(showTimePicker){
            CustomTimePickerDialog(
                onUpdateTime = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                },
                onHideDialog = { showTimePicker = false }
            )
        }
    }


}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
}
