/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sayem.main.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sayem.main.ui.metaitem.MetaItemScreen
import com.sayem.main.ui.scheduler.ScheduleCreateRoute
import com.sayem.main.ui.scheduler.ScheduleDetailRoute
import com.sayem.main.ui.scheduler.SchedulerListRoute
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "scheduler") {
        composable("scheduler") {
            SchedulerListRoute(
                onCreateSchedule = { navController.navigate("create_schedule") },
                onScheduleClick = { id -> navController.navigate("schedule_detail/$id") },
                modifier = Modifier.padding(16.dp)
            )
        }
        composable("create_schedule") {
            ScheduleCreateRoute(
                onScheduleCreated = { navController.popBackStack() },
                modifier = Modifier.padding(16.dp)
            )
        }
        composable(
            "schedule_detail/{scheduleId}",
            arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: return@composable
            ScheduleDetailRoute(
                scheduleId = scheduleId,
                onNavigateUp = { navController.popBackStack() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
