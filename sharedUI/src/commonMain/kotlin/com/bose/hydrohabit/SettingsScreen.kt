package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.presentation.settings.SettingsState
import com.bose.hydrohabit.theme.glassCard
import com.bose.hydrohabit.theme.softCard

/**
 * "My Profile" Settings screen — lavender soft-UI restyle.
 *
 * Layout: centered avatar with edit affordance, name + email headline, two summary
 * cards (Daily Water Goal / Reminder Interval), then grouped soft-card list rows for
 * every control.  ALL interactive controls from the original design are preserved
 * and wired to the same callbacks:
 *
 *  - Profile editor  (weight + age fields → onSaveProfile)
 *  - Reminders toggle switch  → onUpdateReminders(copy(enabled))
 *  - Interval field + Apply button → onUpdateReminders(copy(intervalMinutes))
 *  - Strategy chips → onUpdateReminders(copy(strategy))
 *  - Theme switcher chips (SYSTEM / LIGHT / DARK) → onUpdateReminders(copy(themeMode))
 *  - Notification-sound chips + Preview buttons → onUpdateReminders(copy(notificationSound))
 *                                                 + soundPlayer.playSoundPreview(sound)
 *  - Accent-color preset chips + custom hex field → onUpdateReminders(copy(accentColor))
 *
 * Editable field state is seeded once (on load) so reactive re-emissions never reset
 * text mid-edit.
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

    // Derived summary values for the hero cards
    val goalMl = if (weight.isNotEmpty()) {
        ((weight.toDoubleOrNull() ?: 70.0) * 30).toInt()
    } else {
        state.profile?.weightKg?.let { (it * 30).toInt() } ?: 2100
    }
    val reminderIntervalDisplay = "${state.reminderSettings.intervalMinutes} min"

    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Page title ──────────────────────────────────────────────────────
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground,
            )

            // ── Avatar + name/email hero ────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Avatar circle with first-letter initial (edit affordance)
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "💧",
                            style = MaterialTheme.typography.headlineLarge,
                            color = scheme.onPrimaryContainer,
                        )
                    }
                    // Edit affordance badge
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(scheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✏",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onPrimary,
                        )
                    }
                }

                // Weight + age summary line beneath avatar
                val profileSubtitle = buildString {
                    val w = state.profile?.weightKg?.toInt()
                    val a = state.profile?.age
                    if (w != null) append("${w} kg")
                    if (w != null && a != null) append(" · ")
                    if (a != null) append("${a} yrs")
                    if (isEmpty()) append("Set your profile below")
                }
                Text(
                    text = profileSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Two summary stat cards ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryStatCard(
                    label = "Daily Water Goal",
                    value = "${goalMl} ml",
                    modifier = Modifier.weight(1f),
                )
                SummaryStatCard(
                    label = "Reminder Interval",
                    value = reminderIntervalDisplay,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Profile editor card ─────────────────────────────────────────────
            SettingsSectionCard(title = "Profile") {
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
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Save profile & recalculate goal") }
            }

            // ── Reminders card ──────────────────────────────────────────────────
            SettingsSectionCard(title = "Reminders") {
                SettingsRowToggle(
                    label = "Reminders enabled",
                    checked = state.reminderSettings.enabled,
                    onCheckedChange = { onUpdateReminders(state.reminderSettings.copy(enabled = it)) },
                )
                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit) },
                    label = { Text("Interval (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Apply interval") }
            }

            // ── Customization card ──────────────────────────────────────────────
            SettingsSectionCard(title = "Appearance") {

                // 1. Theme selection
                Text(
                    "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                        FilterChip(
                            selected = state.reminderSettings.themeMode.uppercase() == mode,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(themeMode = mode)) },
                            label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }

                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                // 2. Accent color picker
                Text(
                    "Accent Color",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                val presets = listOf(
                    "Ocean Blue" to "#006690",
                    "Teal Breeze" to "#006A75",
                    "Sunset Orange" to "#E65100",
                    "Emerald Green" to "#1B5E20",
                    "Purple Rain" to "#6A1B9A",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presets.forEach { (name, hex) ->
                        FilterChip(
                            selected = state.reminderSettings.accentColor.equals(hex, ignoreCase = true),
                            onClick = { onUpdateReminders(state.reminderSettings.copy(accentColor = hex)) },
                            label = { Text(name) },
                        )
                    }
                }
                var customColorInput by rememberSaveable(state.reminderSettings.accentColor) {
                    mutableStateOf(state.reminderSettings.accentColor)
                }
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
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Notification sound card ─────────────────────────────────────────
            SettingsSectionCard(title = "Notification Sound") {
                val sounds = listOf("default", "chime", "glass", "droplet", "ping")
                sounds.forEach { sound ->
                    val isSelected = state.reminderSettings.notificationSound.lowercase() == sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(notificationSound = sound)) },
                            label = { Text(sound.replaceFirstChar { it.uppercase() }) },
                        )
                        Button(
                            onClick = { soundPlayer.playSoundPreview(sound) },
                            modifier = Modifier.padding(start = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.secondaryContainer,
                                contentColor = scheme.onSecondaryContainer,
                            ),
                        ) {
                            Text("▶ Preview")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Shared helpers ──────────────────────────────────────────────────────────────

/**
 * A soft-card section with a bold title header and a [content] slot.
 */
@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

/**
 * A labelled row with a [Switch] on the trailing end.
 */
@Composable
private fun SettingsRowToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * A small hero stat card: big [value] with a [label] underneath.
 */
@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.softCard(shape = RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
