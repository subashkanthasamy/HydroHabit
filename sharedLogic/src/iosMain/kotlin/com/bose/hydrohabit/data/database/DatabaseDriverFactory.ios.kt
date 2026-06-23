package com.bose.hydrohabit.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bose.hydrohabit.db.HydroHabitDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(HydroHabitDatabase.Schema, DATABASE_NAME)
}
