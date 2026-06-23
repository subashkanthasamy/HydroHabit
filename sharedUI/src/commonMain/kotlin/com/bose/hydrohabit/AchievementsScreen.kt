package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.presentation.achievements.AchievementsState
import com.bose.hydrohabit.theme.glassCard

@Composable
fun AchievementsScreen(state: AchievementsState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Achievements (${state.unlockedCount}/${state.achievements.size})",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.achievements, key = { it.id.name }) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val unlockedBgTint = if (achievement.isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
    }
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(
                shape = RoundedCornerShape(18.dp),
                borderWidth = if (achievement.isUnlocked) 1.5.dp else 1.dp
            )
            .background(unlockedBgTint, shape = RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (achievement.isUnlocked) "🏆 ${achievement.title}" else "🔒 ${achievement.title}",
                fontWeight = FontWeight.Bold,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(achievement.description, style = MaterialTheme.typography.bodySmall)
            if (!achievement.isUnlocked) {
                LinearProgressIndicator(progress = { achievement.progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
