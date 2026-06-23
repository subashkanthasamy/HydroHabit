package com.bose.hydrohabit.data.database

import com.bose.hydrohabit.db.HydroHabitDatabase

/** Builds the generated [HydroHabitDatabase] from a platform driver. */
fun createDatabase(driverFactory: DatabaseDriverFactory): HydroHabitDatabase =
    HydroHabitDatabase(driverFactory.createDriver())
