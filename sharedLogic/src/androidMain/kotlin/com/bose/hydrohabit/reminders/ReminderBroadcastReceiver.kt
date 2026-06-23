package com.bose.hydrohabit.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives the AlarmManager alarm and posts the hydration notification. Register in the manifest. */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Time to hydrate"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: "Drink some water"
        HydrationNotifier(context.applicationContext).show(id, title, text)
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
    }
}
