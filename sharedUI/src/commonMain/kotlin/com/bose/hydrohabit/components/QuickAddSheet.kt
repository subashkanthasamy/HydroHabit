package com.bose.hydrohabit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.usecase.QuickAddOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddSheet(
    options: List<QuickAddOption>,
    onPick: (QuickAddOption) -> Unit,
    onCustom: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var customText by remember { mutableStateOf("") }

    // Validation: non-empty input that is not a positive integer
    val isError = customText.isNotEmpty() && (customText.toIntOrNull() == null || customText.toIntOrNull()!! <= 0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // QA-3: use headlineSmall (naturally heavier role) instead of titleLarge + FontWeight.Bold
            Text(
                text = "Add Water",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    // QA-1 + QA-2: replace FilterChip (always selected=false) with SuggestionChip
                    // (one-shot trigger, no selection state). Remove softCard wrapper.
                    SuggestionChip(
                        onClick = { onPick(option) },
                        label = { Text(option.label) },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Custom amount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // QA-5: add persistent floating label; add isError + supportingText validation feedback
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (ml)") },
                    placeholder = { Text("ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a number in ml") }
                    } else null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                )

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        val amount = customText.toIntOrNull() ?: 0
                        if (amount > 0) {
                            onCustom(amount)
                            customText = ""
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Add")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
