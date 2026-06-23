package com.bose.hydrohabit

import android.app.Application
import com.bose.hydrohabit.di.initKoin
import com.bose.hydrohabit.work.ReminderWorkScheduler

class HydroHabitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
        ReminderWorkScheduler.ensureScheduled(this)
    }
}
