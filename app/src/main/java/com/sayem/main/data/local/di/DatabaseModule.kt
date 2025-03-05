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

package com.sayem.main.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.sayem.main.data.local.database.AppDatabase
import com.sayem.main.data.local.database.MetaItemDao
import com.sayem.main.data.local.database.ScheduledAppDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideMetaItemDao(appDatabase: AppDatabase): MetaItemDao {
        return appDatabase.metaItemDao()
    }

    @Provides
    fun provideScheduledAppDao(appDatabase: AppDatabase): ScheduledAppDao {
        return appDatabase.scheduledAppDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_scheduler.db"
        ).addMigrations(
            object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS scheduled_apps (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            packageName TEXT NOT NULL,
                            scheduledTime INTEGER NOT NULL,
                            isExecuted INTEGER NOT NULL DEFAULT 0,
                            isCancelled INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL
                        )
                        """
                    )
                }
            },
            object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Add appName column with default value as package name
                    database.execSQL("ALTER TABLE scheduled_apps ADD COLUMN appName TEXT NOT NULL DEFAULT ''")
                    // Update appName for existing records using package name
                    database.execSQL("UPDATE scheduled_apps SET appName = packageName")
                }
            }
        ).build()
    }
}
