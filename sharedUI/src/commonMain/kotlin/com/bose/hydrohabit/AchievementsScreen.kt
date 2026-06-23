package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.presentation.achievements.AchievementsState
import com.bose.hydrohabit.theme.glassCard
import com.bose.hydrohabit.theme.softCard

@Composable
fun AchievementsScreen(
    state: AchievementsState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    // B4: The root Column has an opaque background which acts as the overlay backdrop
    // (prevents taps passing through to content behind this screen).
    // The old no-op clickable(indication=null){} on top of that was redundant as a
    // tap-absorber — the background + Column already block pointer events — and it
    // swallowed taps with no purpose. Removed per B4; overlay backdrop preserved via
    // the opaque background below.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar: soft-circle back button + title
        AchievementsTopBar(onBack = onBack, unlockedCount = state.unlockedCount, total = state.achievements.size)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
        ) {
            items(state.achievements, key = { it.id.name }) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
private fun AchievementsTopBar(
    onBack: () -> Unit,
    unlockedCount: Int,
    total: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // B1 + B2 + B3: 48dp hit area (was 40dp), circular ripple clip, Back semantics.
        // The 40dp soft-circle visual is preserved inside the 48dp clickable.
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape) // B3: circular ripple
                .clickable(onClick = onBack) // B1: 48dp hit target
                .semantics {
                    contentDescription = "Back" // B2
                    role = Role.Button           // B2
                },
            contentAlignment = Alignment.Center,
        ) {
            // 40dp soft-circle visual — unchanged
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .softCard(shape = CircleShape, elevation = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    // B7: titleMedium already carries its own weight; redundant Bold removed
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall,
                // B7: headlineSmall has its own weight; ExtraBold override removed
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$unlockedCount / $total unlocked",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val isUnlocked = achievement.isUnlocked
    val badgeEmoji = if (isUnlocked) "🏆" else "🔒"
    val cardShape = RoundedCornerShape(22.dp)
    val progressPercent = (achievement.progress * 100).toInt()

    // B5: Raise tint alpha for unlocked cards so title/description text stays legible
    // over the glass surface in both light and dark themes (was 0.30f, raised to 0.45f).
    val bgTint = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    // B2/B6: merged contentDescription announces locked/unlocked state + progress
    val cardDesc = if (isUnlocked) {
        "${achievement.title}, unlocked"
    } else {
        "${achievement.title}, locked, $progressPercent%"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = cardShape, shadowElevation = if (isUnlocked) 10.dp else 6.dp)
            .background(bgTint, shape = cardShape)
            .semantics(mergeDescendants = true) { contentDescription = cardDesc }, // B2/B6
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Badge emoji in a soft circle
            // B5: Raise badge-circle alpha so it's visually distinct (was 0.12f / 0.06f)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // B2: badge emoji is decorative — the merged card semantics carry meaning
                Text(
                    text = badgeEmoji,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { hideFromAccessibility() },
                )
            }

            // Title
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelLarge,
                // B7: labelLarge has its own weight; redundant Bold removed
                color = if (isUnlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
            )

            // Description
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Progress bar for locked achievements
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(2.dp))
                // B8: use surfaceVariant as track for better ≥3:1 contrast vs primary in dark theme
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Unlock indicator
                Text(
                    text = "✓ Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    // B7: labelSmall has its own weight; redundant SemiBold removed
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
