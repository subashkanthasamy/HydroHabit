package com.bose.hydrohabit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
        // BN-1: semantics role + contentDescription; BN-2: size increased to 64dp for ≥48dp touch target with offset
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(64.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(scheme.secondary, scheme.primary)))
                .clickable { onFabClick() }
                .semantics {
                    role = Role.Button
                    contentDescription = "Add water"
                },
            contentAlignment = Alignment.Center
        ) {
            // BN-1: emoji is purely decorative; action is described by the parent semantics
            Text(
                text = "💧",
                modifier = Modifier
                    .rotate(-45f)
                    .clearAndSetSemantics {},
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
    // BN-4: animate tint color over 200ms instead of instant swap
    val tint by animateColorAsState(
        targetValue = if (selected) scheme.primary else scheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "navTint"
    )
    // BN-3: heightIn(min=48.dp) ensures touch target ≥48dp
    // BN-5: mergeDescendants + role + selected + contentDescription on column; icon text marked decorative
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect(item.id) }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Tab
                this.selected = selected
                contentDescription = item.label
            }
    ) {
        // BN-5: icon is decorative; contentDescription on the column is sufficient
        Text(
            text = item.icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
