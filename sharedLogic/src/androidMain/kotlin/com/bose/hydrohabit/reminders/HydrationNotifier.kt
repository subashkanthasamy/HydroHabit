package com.bose.hydrohabit.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

import org.koin.java.KoinJavaComponent.get
import kotlinx.coroutines.runBlocking
import com.bose.hydrohabit.domain.repository.ReminderSettingsRepository

/** Creates the notification channel and posts hydration reminders. */
class HydrationNotifier(private val context: Context) {

    private fun getSoundUri(soundName: String): android.net.Uri {
        return when (soundName.lowercase()) {
            "chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "glass" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            "droplet" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            "ping" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun ensureChannel(soundName: String = "default") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val dynamicChannelId = "${CHANNEL_ID}_${soundName.lowercase()}"
            if (manager.getNotificationChannel(dynamicChannelId) == null) {
                try {
                    manager.notificationChannels.forEach { existingChannel ->
                        if (existingChannel.id.startsWith(CHANNEL_ID) && existingChannel.id != dynamicChannelId) {
                            manager.deleteNotificationChannel(existingChannel.id)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                val channel = NotificationChannel(dynamicChannelId, "Hydration Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reminders to drink water throughout the day"
                    enableVibration(true)
                    val soundUri = getSoundUri(soundName)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun show(id: Int, title: String, text: String) {
        val repo = get<ReminderSettingsRepository>(ReminderSettingsRepository::class.java)
        val settings = runBlocking { repo.getSettings() }
        val soundName = settings.notificationSound

        val finalChannelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dynamicChannelId = "${CHANNEL_ID}_${soundName.lowercase()}"
            ensureChannel(soundName)
            dynamicChannelId
        } else {
            CHANNEL_ID
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, finalChannelId)
        } else {
            val soundUri = getSoundUri(soundName)
            android.app.Notification.Builder(context)
                .setSound(soundUri)
                .setDefaults(android.app.Notification.DEFAULT_VIBRATE)
        }
        val notification = builder
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "hydration_reminders"
    }
}
