package com.bose.hydrohabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.theme.softCard

data class NavItem(val id: String, val label: String, val icon: String)

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .softCard(RoundedCornerShape(28.dp), elevation = 14.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.take(2).forEach { NavTab(it, it.id == selectedId, onSelect, scheme) }
            Spacer(Modifier.width(52.dp))
            items.drop(2).forEach { NavTab(it, it.id == selectedId, onSelect, scheme) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(60.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(scheme.secondary, scheme.primary)))
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💧",
                modifier = Modifier.rotate(-45f),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun NavTab(
    item: NavItem,
    selected: Boolean,
    onSelect: (String) -> Unit,
    scheme: ColorScheme
) {
    val tint = if (selected) scheme.primary else scheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect(item.id) }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(item.icon, style = MaterialTheme.typography.titleMedium)
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
