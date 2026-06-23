package com.bose.hydrohabit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.presentation.settings.SettingsState
import com.bose.hydrohabit.theme.glassCard

/**
 * Settings form. Editable field state is hoisted and **seeded once** when data finishes loading, so
 * reactive re-emissions (e.g. after a save) never reset the text or steal focus mid-edit. The whole
 * form scrolls and applies `imePadding()` so fields stay visible above the keyboard.
 */
@Composable
fun SettingsScreen(
    state: SettingsState,
    soundPlayer: com.bose.hydrohabit.util.SoundPlayer,
    onSaveProfile: (Double, Int) -> Unit,
    onUpdateReminders: (ReminderSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var weight by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var interval by rememberSaveable { mutableStateOf("") }
    var seeded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !seeded) {
            weight = state.profile?.weightKg?.toInt()?.toString() ?: "70"
            age = state.profile?.age?.toString() ?: "30"
            interval = state.reminderSettings.intervalMinutes.toString()
            seeded = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Box(Modifier.fillMaxWidth().glassCard(shape = RoundedCornerShape(20.dp))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Profile", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter(Char::isDigit) },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter(Char::isDigit) },
                    label = { Text("Age") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val w = weight.toDoubleOrNull()
                        val a = age.toIntOrNull()
                        if (w != null && a != null) onSaveProfile(w, a)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save profile & recalculate goal") }
            }
        }

        Box(Modifier.fillMaxWidth().glassCard(shape = RoundedCornerShape(20.dp))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Reminders", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Enabled")
                    Switch(
                        checked = state.reminderSettings.enabled,
                        onCheckedChange = { onUpdateReminders(state.reminderSettings.copy(enabled = it)) },
                    )
                }
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit) },
                    label = { Text("Interval (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderStrategy.entries.forEach { strategy ->
                        FilterChip(
                            selected = state.reminderSettings.strategy == strategy,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(strategy = strategy)) },
                            label = { Text(strategy.name.lowercase().replace('_', ' ')) },
                        )
                    }
                }
                Button(
                    onClick = {
                        interval.toIntOrNull()?.takeIf { it > 0 }
                            ?.let { onUpdateReminders(state.reminderSettings.copy(intervalMinutes = it)) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Apply interval") }
            }
        }

        Box(Modifier.fillMaxWidth().glassCard(shape = RoundedCornerShape(20.dp))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Customization", fontWeight = FontWeight.Bold)
                
                // 1. Theme Selection
                Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                        FilterChip(
                            selected = state.reminderSettings.themeMode.uppercase() == mode,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(themeMode = mode)) },
                            label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                // 2. Notification Sound Selection
                Text("Notification Sound", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                val sounds = listOf("default", "chime", "glass", "droplet", "ping")
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sounds.forEach { sound ->
                        val isSelected = state.reminderSettings.notificationSound.lowercase() == sound
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateReminders(state.reminderSettings.copy(notificationSound = sound)) },
                                label = { Text(sound.replaceFirstChar { it.uppercase() }) }
                            )
                            Button(
                                onClick = { soundPlayer.playSoundPreview(sound) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("▶ Play Preview")
                            }
                        }
                    }
                }

                // 3. Accent Color Picker
                Text("Accent Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                
                // Predefined presets
                val presets = listOf(
                    "Ocean Blue" to "#006690",
                    "Teal Breeze" to "#006A75",
                    "Sunset Orange" to "#E65100",
                    "Emerald Green" to "#1B5E20",
                    "Purple Rain" to "#6A1B9A"
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (name, hex) ->
                        val isSelected = state.reminderSettings.accentColor.equals(hex, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(accentColor = hex)) },
                            label = { Text(name) }
                        )
                    }
                }
                
                // Custom Color Input
                var customColorInput by rememberSaveable(state.reminderSettings.accentColor) { mutableStateOf(state.reminderSettings.accentColor) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customColorInput,
                        onValueChange = {
                            customColorInput = it
                            if (it.length == 7 && it.startsWith("#")) {
                                onUpdateReminders(state.reminderSettings.copy(accentColor = it))
                            }
                        },
                        label = { Text("Custom Color Hex (e.g. #006690)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
