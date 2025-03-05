package com.sayem.main.ui.scheduler.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.sayem.main.R
import com.sayem.main.ui.scheduler.ScheduleUiModel

@Composable
fun SchedulerListRoute(
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SchedulerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SchedulerListScreen(
        uiState = uiState,
        onCreateSchedule = onCreateSchedule,
        onScheduleClick = onScheduleClick,
        onCancelSchedule = viewModel::cancelSchedule,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerListScreen(
    uiState: SchedulerListUiState,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    onCancelSchedule: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Scheduled Apps")
                },
                actions = {
                    IconButton(
                        onClick = onCreateSchedule
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },

                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),



            )

        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSchedule,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create schedule"
                )
            }

        }
    ){ paddingValues ->
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    SchedulerListUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is SchedulerListUiState.Success -> {
                        if (uiState.schedules.isEmpty()) {
                            Text(
                                text = "No scheduled apps",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = uiState.schedules,
                                    key = { it.id }
                                ) { schedule ->
                                    ScheduleItem2(
                                        schedule = schedule,
                                        onClick = { onScheduleClick(schedule.id) },
                                        onCancel = { onCancelSchedule(schedule.id) },
                                        //                                modifier = Modifier
                                        //                                    .fillMaxWidth()
                                        //                                    .padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }

                        //                    FloatingActionButton(
                        //                        onClick = onCreateSchedule,
                        //                        modifier = Modifier
                        //                            .align(Alignment.BottomEnd)
                        //                            .padding(16.dp)
                        //                    ) {
                        //                        Icon(
                        //                            imageVector = Icons.Default.Add,
                        //                            contentDescription = "Create schedule"
                        //                        )
                        //                    }
                    }
                }
            }
        }


    }
}

@Composable
fun ScheduleItem(
    schedule: ScheduleUiModel,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                schedule.appIcon?.let { icon ->
                    Image(
                        painter = rememberDrawablePainter(drawable = icon),
                        contentDescription = "App icon for ${schedule.appName}",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = schedule.appName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = schedule.scheduledTime,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        schedule.isExecuted -> "Executed"
                        schedule.isCancelled -> "Cancelled"
                        else -> "Scheduled"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (!schedule.isExecuted && !schedule.isCancelled) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ScheduleItemPreview() {
    ScheduleItem(
        schedule = ScheduleUiModel(
            id = 1,
            appIcon = null,
            appName = "App Name",
            scheduledTime = "Scheduled time",
            isExecuted = false,
            isCancelled = false,
            packageName = "ABC"
        ),
        onClick = {},
        onCancel = {}
    )
}

@Composable
fun ScheduleItem2(schedule: ScheduleUiModel, onClick: () -> Unit, onCancel: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onClick()
        },
        headlineContent = {
            Text(schedule.appName)
        },
        supportingContent = {

            when {
                schedule.isExecuted -> {
                    Badge(
                        modifier = Modifier.padding(vertical = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            text = "Executed"
                        )
                    }


                }

                schedule.isCancelled -> {
                    Badge(
                        modifier = Modifier.padding(vertical = 8.dp),

                        ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            text = "Cancelled"
                        )
                    }


                }

                else -> {
                    Badge(
                        modifier = Modifier.padding(vertical = 8.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            text = "Scheduled"
                        )
                    }
                }
            }
        },
        overlineContent = {
            Text(schedule.scheduledTime)
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
            if (!schedule.isExecuted && !schedule.isCancelled) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }



    )
}

@Preview
@Composable
fun ScheduleItem2Preview() {
    ScheduleItem2(
        schedule = ScheduleUiModel(
            id = 1,
            appIcon = null,
            appName = "App Name",
            scheduledTime = "11:32 PM, 12/12/2021",
            isExecuted = false,
            isCancelled = false,
            packageName = "ABC"
        ),
        onClick = {},
        onCancel = {}
    )
}