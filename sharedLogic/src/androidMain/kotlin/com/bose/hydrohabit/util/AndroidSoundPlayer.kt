package com.bose.hydrohabit.util

import android.content.Context
import android.media.RingtoneManager

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    override fun playSoundPreview(soundName: String) {
        val uri = when (soundName.lowercase()) {
            "chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "glass" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            "droplet" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            "ping" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        try {
            activeRingtone?.stop()
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
            activeRingtone = ringtone
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var activeRingtone: android.media.Ringtone? = null
    }
}
