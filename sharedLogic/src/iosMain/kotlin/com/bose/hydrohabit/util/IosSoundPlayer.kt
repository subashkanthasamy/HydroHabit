package com.bose.hydrohabit.util

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.SystemSoundID

class IosSoundPlayer : SoundPlayer {
    override fun playSoundPreview(soundName: String) {
        val soundId: SystemSoundID = when (soundName.lowercase()) {
            "chime" -> 1009u
            "glass" -> 1054u
            "droplet" -> 1005u
            "ping" -> 1007u
            else -> 1000u
        }
        AudioServicesPlaySystemSound(soundId)
    }
}
