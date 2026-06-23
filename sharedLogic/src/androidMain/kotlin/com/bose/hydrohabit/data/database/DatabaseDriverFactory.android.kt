package com.bose.hydrohabit.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.bose.hydrohabit.db.HydroHabitDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(HydroHabitDatabase.Schema, context, DATABASE_NAME)
}
