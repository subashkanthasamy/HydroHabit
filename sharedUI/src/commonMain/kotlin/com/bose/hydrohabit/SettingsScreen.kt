package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.presentation.settings.SettingsState
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.bose.hydrohabit.theme.glassCard
import com.bose.hydrohabit.theme.softCard

private val HEX_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

/**
 * "My Profile" Settings screen — lavender soft-UI restyle.
 *
 * Layout: centered avatar, name + email headline, two summary cards (Daily Water Goal /
 * Reminder Interval), then grouped soft-card list rows for every control.
 * ALL interactive controls from the original design are preserved and wired to the
 * same callbacks:
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
@OptIn(ExperimentalLayoutApi::class)
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

    // Derived summary values for the hero cards — use the real calculator so this matches Home.
    val goalMl = state.profile?.let { p ->
        HydrationGoalCalculator()
            .calculate(p, Clock.System.todayIn(TimeZone.currentSystemDefault()))
            .targetMl
    } ?: 2100
    val reminderIntervalDisplay = "${state.reminderSettings.intervalMinutes} min"

    // Validation helpers
    val weightVal = weight.toDoubleOrNull()
    val ageVal = age.toIntOrNull()
    val weightError = weight.isNotEmpty() && (weightVal == null || weightVal <= 0 || weightVal > 300)
    val ageError = age.isNotEmpty() && (ageVal == null || ageVal <= 0 || ageVal > 120)

    val intervalVal = interval.toIntOrNull()
    val intervalError = interval.isNotEmpty() && (intervalVal == null || intervalVal <= 0)

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
                color = scheme.onBackground,
            )

            // ── Avatar + name/email hero ────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Avatar circle with first-letter initial — pencil badge removed (C3: false affordance)
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
                    isError = weightError,
                    supportingText = if (weightError) {
                        { Text("Enter a weight between 1 and 300 kg") }
                    } else {
                        { Text("Used to calculate your daily goal") }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter(Char::isDigit) },
                    label = { Text("Age") },
                    singleLine = true,
                    isError = ageError,
                    supportingText = if (ageError) {
                        { Text("Enter an age between 1 and 120") }
                    } else {
                        { Text("Used to adjust hydration recommendation") }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val w = weight.toDoubleOrNull()
                        val a = age.toIntOrNull()
                        if (w != null && a != null) onSaveProfile(w, a)
                    },
                    enabled = !weightError && !ageError && weight.isNotEmpty() && age.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
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
                // C12: use full outlineVariant, no alpha reduction
                HorizontalDivider(color = DividerDefaults.color)
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit) },
                    label = { Text("Interval (minutes)") },
                    singleLine = true,
                    isError = intervalError,
                    supportingText = if (intervalError) {
                        { Text("Interval must be greater than 0") }
                    } else {
                        { Text("How often to remind you to drink water") }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                // C1: FlowRow so strategy chips wrap on narrow screens
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReminderStrategy.entries.forEach { strategy ->
                        FilterChip(
                            selected = state.reminderSettings.strategy == strategy,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(strategy = strategy)) },
                            label = { Text(strategy.name.lowercase().replace('_', ' ')) },
                        )
                    }
                }
                // C6: secondary sub-action → FilledTonalButton
                FilledTonalButton(
                    onClick = {
                        interval.toIntOrNull()?.takeIf { it > 0 }
                            ?.let { onUpdateReminders(state.reminderSettings.copy(intervalMinutes = it)) }
                    },
                    enabled = !intervalError && interval.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Apply interval") }
            }

            // ── Customization card ──────────────────────────────────────────────
            SettingsSectionCard(title = "Appearance") {

                // 1. Theme selection
                Text(
                    "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurface,
                )
                // C1: FlowRow for theme chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                        FilterChip(
                            selected = state.reminderSettings.themeMode.uppercase() == mode,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(themeMode = mode)) },
                            label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }

                // C12: use full outlineVariant, no alpha reduction
                HorizontalDivider(color = DividerDefaults.color)

                // 2. Accent color picker
                Text(
                    "Accent Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurface,
                )
                val presets = listOf(
                    "Ocean Blue" to "#006690",
                    "Teal Breeze" to "#006A75",
                    "Sunset Orange" to "#E65100",
                    "Emerald Green" to "#1B5E20",
                    "Purple Rain" to "#6A1B9A",
                )
                // C1: FlowRow so all 5 preset chips wrap rather than overflow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presets.forEach { (name, hex) ->
                        FilterChip(
                            selected = state.reminderSettings.accentColor.equals(hex, ignoreCase = true),
                            onClick = { onUpdateReminders(state.reminderSettings.copy(accentColor = hex)) },
                            label = { Text(name) },
                        )
                    }
                }

                // C4/C5/C8: hex validation, keyboard options, char filtering, state-race fix
                // customColorInput is NOT keyed on accentColor to avoid preset→field race.
                // We only emit onUpdateReminders when the value passes the regex.
                var customColorInput by rememberSaveable { mutableStateOf(state.reminderSettings.accentColor) }
                // Sync hex field when a preset chip changes accentColor (race-safe: typed
                // valid hex already updates accentColor, so re-assigning the same value is a no-op).
                LaunchedEffect(state.reminderSettings.accentColor) {
                    customColorInput = state.reminderSettings.accentColor
                }
                val hexError = customColorInput.isNotEmpty() && !HEX_REGEX.matches(customColorInput)
                OutlinedTextField(
                    value = customColorInput,
                    onValueChange = { raw ->
                        // Allow only '#' and hex digit characters, max 7 chars
                        val filtered = raw.filter { it == '#' || it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                            .take(7)
                        customColorInput = filtered
                        // Only emit a valid hex color
                        if (HEX_REGEX.matches(filtered)) {
                            onUpdateReminders(state.reminderSettings.copy(accentColor = filtered))
                        }
                    },
                    label = { Text("Custom Color Hex (e.g. #6C5CE7)") },
                    singleLine = true,
                    isError = hexError,
                    supportingText = if (hexError) {
                        { Text("Must be a valid hex: #RRGGBB (6 hex digits)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Notification sound card ─────────────────────────────────────────
            SettingsSectionCard(title = "Notification Sound") {
                val sounds = listOf("default", "chime", "glass", "droplet", "ping")
                sounds.forEach { sound ->
                    val isSelected = state.reminderSettings.notificationSound.lowercase() == sound
                    // C9: weight(1f, fill=false) on the chip so it doesn't collide with Preview button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateReminders(state.reminderSettings.copy(notificationSound = sound)) },
                            label = { Text(sound.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        // C2/C11: FilledTonalButton (maps to secondaryContainer natively) with 48dp min height
                        FilledTonalButton(
                            onClick = { soundPlayer.playSoundPreview(sound) },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(16.dp),
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
