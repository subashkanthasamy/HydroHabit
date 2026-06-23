package com.bose.hydrohabit.data.database

import app.cash.sqldelight.db.SqlDriver

/** Platform seam for creating the SQLDelight driver. Implemented in androidMain / iosMain. */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

const val DATABASE_NAME = "hydrohabit.db"
