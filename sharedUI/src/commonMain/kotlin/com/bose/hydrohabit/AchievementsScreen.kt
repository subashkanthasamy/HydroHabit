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
import androidx.compose.ui.text.font.FontWeight
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {},
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
        // Soft-circle back button
        Box(
            modifier = Modifier
                .size(40.dp)
                .softCard(shape = CircleShape, elevation = 6.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
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

    // Tint: subtle primary tint for unlocked, muted for locked
    val bgTint = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = cardShape, shadowElevation = if (isUnlocked) 10.dp else 6.dp)
            .background(bgTint, shape = cardShape),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Badge emoji in a soft circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(badgeEmoji, style = MaterialTheme.typography.titleMedium)
            }

            // Title
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
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
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
                Text(
                    text = "${(achievement.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Unlock indicator
                Text(
                    text = "✓ Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
