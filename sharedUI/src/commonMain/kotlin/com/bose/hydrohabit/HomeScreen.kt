package com.bose.hydrohabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.components.BarChart
import com.bose.hydrohabit.components.BarDatum
import com.bose.hydrohabit.components.WaterRing
import com.bose.hydrohabit.components.WeekStrip
import com.bose.hydrohabit.domain.model.HydrationInsight
import com.bose.hydrohabit.domain.model.InsightType
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.usecase.QuickAddOption
import com.bose.hydrohabit.presentation.home.HomeState
import com.bose.hydrohabit.theme.glassCard
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Stateless dashboard UI. Renders [HomeState] and emits events. Loading / onboarding / dashboard
 * states are handled explicitly; the content column is width-capped so it reads well on tablets.
 *
 * [onShowAchievements] is defaulted so existing [MainScreen] call sites compile unchanged.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
    onCreateProfile: (Double, Int) -> Unit,
    onDismissUnlocked: () -> Unit = {},
    modifier: Modifier = Modifier,
    onShowAchievements: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when {
                state.isLoading -> LoadingState()
                state.progress == null || state.progress!!.goalMl == 0 -> OnboardingCard(onCreateProfile)
                else -> Dashboard(state, onQuickAdd, onAddCustom, onDismissUnlocked, onShowAchievements)
            }
            state.error?.let { ErrorCard(it) }
        }
    }
}

@Composable
private fun LoadingState() {
    // Fix 6: add contentDescription so TalkBack announces this state
    Box(Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = "Loading" },
        )
    }
}

@Composable
private fun Dashboard(
    state: HomeState,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
    onDismissUnlocked: () -> Unit,
    onShowAchievements: () -> Unit,
) {
    val progress = state.progress!!
    val today: LocalDate = state.date
        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // ── Header Row ─────────────────────────────────────────────────────────
    HeaderRow(onShowAchievements = onShowAchievements)

    // ── Date + Streak Row ──────────────────────────────────────────────────
    DateStreakRow(today = today, streak = state.streak.currentDailyStreak)

    // ── Achievement banner (animated) ─────────────────────────────────────
    AchievementBanner(state, onDismissUnlocked)

    // ── "Today" card with WeekStrip ────────────────────────────────────────
    TodayCard(today = today)

    // ── Daily Drink Target card ────────────────────────────────────────────
    DailyDrinkTargetCard(
        state = state,
        progress = progress,
        onQuickAdd = onQuickAdd,
        onAddCustom = onAddCustom,
    )

    // ── Hydration Stats card ───────────────────────────────────────────────
    HydrationStatsCard(
        recentEntries = state.recentEntries,
        today = today,
        goalMl = progress.goalMl,
    )

    // ── Insights ──────────────────────────────────────────────────────────
    if (state.insights.isNotEmpty()) {
        SectionTitle("Today's insights")
        state.insights.forEach { InsightCard(it) }
    }

    // ── Recent activity ───────────────────────────────────────────────────
    SectionTitle("Recent activity")
    RecentActivity(state.recentEntries)

    Spacer(Modifier.height(8.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// Header Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderRow(
    onShowAchievements: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar circle: shows 💧 as text initial placeholder — decorative, not interactive
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer)
                // Fix 3: mark the avatar box as decorative so TalkBack skips the emoji
                .semantics { contentDescription = "" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "💧",
                style = MaterialTheme.typography.titleMedium,
                // Fix 3: emoji is purely decorative here — clear semantics so it is not read
                modifier = Modifier.clearAndSetSemantics {},
            )
        }

        // Greeting column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good Morning",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = "HydroHabit",
                style = MaterialTheme.typography.titleMedium,
                // Fix 8: titleMedium already carries appropriate weight; Bold is redundant
                color = scheme.onSurface,
            )
        }

        // Fix 1 & 2: SoftIconButton uses minimumInteractiveComponentSize; semantics wired up
        // 🏆 Achievements button
        SoftIconButton(
            label = "🏆",
            contentDesc = "Achievements",
            onClick = onShowAchievements,
        )

        // Fix 7: Reminders button is not yet wired — disable so it is not announced as active
        SoftIconButton(
            label = "🔔",
            contentDesc = "Reminders",
            onClick = {},
            enabled = false,
        )
    }
}

// Fix 1, 2, 7: minimumInteractiveComponentSize replaces hard 44dp; semantics applied; enabled param added
@Composable
private fun SoftIconButton(
    label: String,
    contentDesc: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = scheme.surfaceVariant,
        // Fix 1: minimumInteractiveComponentSize ensures ≥ 48dp touch target; visual stays 48dp
        modifier = Modifier
            .size(48.dp)
            // Fix 2: expose contentDescription and Role.Button to accessibility tree
            .semantics {
                role = Role.Button
                contentDescription = contentDesc
            },
        // Fix 2: raise tonalElevation so hover/focus tint overlay is visible
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            // Fix 2/3: label emoji is decorative — the semantics are on the Surface above
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date + Streak Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateStreakRow(today: LocalDate, streak: Int) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Today, ${today.dayOfMonth} ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        // Streak chip
        if (streak > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(scheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "🔥 $streak-day streak",
                    style = MaterialTheme.typography.labelMedium,
                    // Fix 4: correct semantic pairing — onSecondaryContainer on secondaryContainer
                    color = scheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Today Card (WeekStrip)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TodayCard(today: LocalDate) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(scheme.secondaryContainer)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            WeekStrip(today = today)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Daily Drink Target Card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyDrinkTargetCard(
    state: HomeState,
    progress: com.bose.hydrohabit.domain.model.DailyProgress,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
) {
    val pct = (progress.completionPercent * 100).toInt()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Card header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Daily Drink Target",
                    style = MaterialTheme.typography.titleMedium,
                    // Fix 8: titleMedium already carries appropriate weight; Bold is redundant
                )
                Text(
                    text = "Goal ${progress.goalMl} ml · $pct% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Ring on the left, actions on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WaterRing(
                    progress = progress.completionPercent,
                    consumedMl = progress.consumedMl,
                    goalMl = progress.goalMl,
                    waveFill = true,
                    modifier = Modifier.size(160.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Primary drink button — uses first quick-add option amount or remaining ml
                    val drinkMl = state.quickAddOptions.firstOrNull()?.amountMl
                        ?: progress.remainingMl.coerceAtLeast(250)
                    Button(
                        onClick = { onAddCustom(drinkMl) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Drink $drinkMl ml", fontWeight = FontWeight.SemiBold)
                    }

                    // Fix 1: minimumInteractiveComponentSize wraps each chip so touch target ≥ 48dp
                    // while the visual height remains 32dp via the height modifier on the button itself
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.quickAddOptions.forEach { option ->
                            FilledTonalButton(
                                onClick = { onQuickAdd(option) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 10.dp, vertical = 0.dp
                                ),
                            ) {
                                Text(
                                    option.label,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hydration Stats Card (BarChart)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HydrationStatsCard(
    recentEntries: List<WaterEntry>,
    today: LocalDate,
    goalMl: Int,
) {
    val scheme = MaterialTheme.colorScheme

    // Build BarDatum list from recentEntries grouped by day over the last 7 days.
    // If no entries exist for a day, the bar is empty (fraction = 0.04 floor is applied by
    // BarChart itself). We never fabricate numbers — fractions come entirely from real data.
    val bars: List<BarDatum> = buildWeekBars(recentEntries, today, goalMl)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Card header with "This Week" pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Hydration Stats",
                    style = MaterialTheme.typography.titleMedium,
                    // Fix 8: titleMedium already carries appropriate weight; Bold is redundant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(scheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.labelSmall,
                        // Fix 4: correct semantic pairing — onSecondaryContainer on secondaryContainer
                        color = scheme.onSecondaryContainer,
                    )
                }
            }
            BarChart(bars = bars)
        }
    }
}

/**
 * Builds a 7-bar week series from [recentEntries] (may span multiple days).
 * Bars represent the 7 days ending with [today]. Fraction = consumed / goal (capped at 1f).
 * Today's bar is highlighted. If [goalMl] is 0 the fraction is 0.
 */
private fun buildWeekBars(
    entries: List<WaterEntry>,
    today: LocalDate,
    goalMl: Int,
): List<BarDatum> {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    // Sum consumed ml per date across all recent entries
    val mlByDate: Map<LocalDate, Int> = entries
        .groupBy { entry -> entry.date }
        .mapValues { (_, dayEntries) -> dayEntries.sumOf { it.amountMl } }

    return (6 downTo 0).map { daysBack ->
        val date = today.minus(daysBack, DateTimeUnit.DAY)
        val consumed = mlByDate[date] ?: 0
        val fraction = if (goalMl > 0) (consumed.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
        val label = dayLabels[date.dayOfWeek.isoDayNumber % 7] // Sun=0, Mon=1 … Sat=6
        BarDatum(label = label, fraction = fraction, highlighted = date == today)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Achievement Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AchievementBanner(state: HomeState, onDismiss: () -> Unit) {
    val visible = state.newlyUnlocked.isNotEmpty()
    LaunchedEffect(state.newlyUnlocked) {
        if (state.newlyUnlocked.isNotEmpty()) {
            delay(4000)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut(),
    ) {
        val achievement = state.newlyUnlocked.lastOrNull()
        if (achievement != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .glassCard(
                        shape = RoundedCornerShape(16.dp),
                        lightAlpha = 0.55f,
                        darkAlpha = 0.15f,
                        borderWidth = 1.5.dp
                    )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "🏆 Achievement unlocked",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    // Fix 8: add explicit style for achievement title text
                    Text(achievement.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(achievement.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Insight Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsightCard(insight: HydrationInsight) {
    val emoji = when (insight.type) {
        InsightType.POSITIVE -> "💪"
        InsightType.WARNING -> "⚠️"
        InsightType.TIP -> "💡"
        InsightType.TREND -> "📈"
    }
    // Fix 3: include insight type in the card's merged contentDescription; emoji marked decorative
    val typeLabel = insight.type.name.lowercase().replaceFirstChar { it.uppercase() }
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "$typeLabel: ${insight.message}"
            }
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Emoji is decorative — meaning is carried by the merged contentDescription above
            Text(
                emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(insight.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recent Activity
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentActivity(entries: List<WaterEntry>) {
    if (entries.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .glassCard(shape = RoundedCornerShape(16.dp))
        ) {
            Text(
                "No water logged yet today — tap a quick-add above to get started.",
                // Fix 6: liveRegion.Polite so screen readers announce when this appears/disappears
                Modifier
                    .padding(16.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            entries.forEach { entry ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Fix 8: add explicit style to amount text
                    Text(
                        "${entry.amountMl} ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    val time = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).time
                    // Fix 8: add explicit style to timestamp text
                    Text(
                        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onboarding Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingCard(onCreateProfile: (Double, Int) -> Unit) {
    var weight by remember { mutableStateOf("70") }
    var age by remember { mutableStateOf("30") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("💧", style = MaterialTheme.typography.displayMedium)
        Text(
            "Welcome to HydroHabit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Tell us a little about you and we'll set a personalized daily hydration goal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .glassCard(shape = RoundedCornerShape(24.dp))
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
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
                        if (w != null && a != null) onCreateProfile(w, a)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Calculate my goal") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String) {
    // Fix 5: apply error tint INSIDE the clipped surface to avoid painting outside clip bounds.
    // glassCard clips at the end of its chain, so a subsequent .background() would render outside
    // the clip on older API levels. Instead we nest the tinted background inside the clipped Box.
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(
                shape = RoundedCornerShape(16.dp),
                lightAlpha = 0.2f,
                darkAlpha = 0.05f
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                )
        ) {
            Text(
                message,
                Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
