package com.bose.hydrohabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.components.WaterRing
import com.bose.hydrohabit.domain.model.HydrationInsight
import com.bose.hydrohabit.domain.model.InsightType
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.usecase.QuickAddOption
import com.bose.hydrohabit.presentation.home.HomeState
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Stateless dashboard UI. Renders [HomeState] and emits events. Loading / onboarding / dashboard
 * states are handled explicitly; the content column is width-capped so it reads well on tablets.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
    onCreateProfile: (Double, Int) -> Unit,
    onDismissUnlocked: () -> Unit = {},
    modifier: Modifier = Modifier,
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
                else -> Dashboard(state, onQuickAdd, onAddCustom, onDismissUnlocked)
            }
            state.error?.let { ErrorCard(it) }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Dashboard(
    state: HomeState,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
    onDismissUnlocked: () -> Unit,
) {
    val progress = state.progress!!

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            if (progress.isCompleted) "Goal reached! 🎉" else "Stay hydrated 💧",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            state.date?.toString() ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    AchievementBanner(state, onDismissUnlocked)

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        WaterRing(
            progress = progress.completionPercent,
            consumedMl = progress.consumedMl,
            goalMl = progress.goalMl,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile("Goal", "${progress.goalMl} ml", Modifier.weight(1f))
        StatTile("Remaining", "${progress.remainingMl} ml", Modifier.weight(1f))
        StatTile("Streak", "🔥 ${state.streak.currentDailyStreak}", Modifier.weight(1f))
    }

    QuickAddSection(state.quickAddOptions, onQuickAdd, onAddCustom)

    if (state.insights.isNotEmpty()) {
        SectionTitle("Today's insights")
        state.insights.forEach { InsightCard(it) }
    }

    SectionTitle("Recent activity")
    RecentActivity(state.recentEntries)

    Spacer(Modifier.height(8.dp))
}

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
            ElevatedCard(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🏆 Achievement unlocked", style = MaterialTheme.typography.labelLarge)
                    Text(achievement.title, fontWeight = FontWeight.Bold)
                    Text(achievement.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAddSection(
    options: List<QuickAddOption>,
    onQuickAdd: (QuickAddOption) -> Unit,
    onAddCustom: (Int) -> Unit,
) {
    SectionTitle("Quick add")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilledTonalButton(onClick = { onQuickAdd(option) }, modifier = Modifier.weight(1f)) {
                Text(option.label)
            }
        }
    }
    var custom by remember { mutableStateOf("") }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = custom,
            onValueChange = { custom = it.filter(Char::isDigit) },
            label = { Text("Custom ml") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Button(onClick = { custom.toIntOrNull()?.takeIf { it > 0 }?.let { onAddCustom(it); custom = "" } }) {
            Text("Add")
        }
    }
}

@Composable
private fun InsightCard(insight: HydrationInsight) {
    val emoji = when (insight.type) {
        InsightType.POSITIVE -> "💪"
        InsightType.WARNING -> "⚠️"
        InsightType.TIP -> "💡"
        InsightType.TREND -> "📈"
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(insight.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RecentActivity(entries: List<WaterEntry>) {
    if (entries.isEmpty()) {
        Card(Modifier.fillMaxWidth()) {
            Text(
                "No water logged yet today — tap a quick-add above to get started.",
                Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            entries.forEach { entry ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${entry.amountMl} ml", fontWeight = FontWeight.Bold)
                    val time = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).time
                    Text(
                        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingCard(onCreateProfile: (Double, Int) -> Unit) {
    var weight by remember { mutableStateOf("70") }
    var age by remember { mutableStateOf("30") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("💧", style = MaterialTheme.typography.displayMedium)
        Text("Welcome to HydroHabit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Tell us a little about you and we'll set a personalized daily hydration goal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ElevatedCard(Modifier.fillMaxWidth()) {
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

@Composable
private fun ErrorCard(message: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            message,
            Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
