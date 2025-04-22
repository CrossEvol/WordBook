package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.model.FilterOption

/**
 * A dropdown menu allowing users to select which parts of a WordItem to display.
 *
 * @param expanded Whether the dropdown is currently visible.
 * @param onDismissRequest Callback when the dropdown should be dismissed.
 * @param selectedOptions The currently selected set of FilterOptions.
 * @param onOptionToggle Callback when a filter option's state is changed.
 */
@Composable
fun FilterDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedOptions: Set<FilterOption>,
    onOptionToggle: (FilterOption, Boolean) -> Unit
) {
    // Define the order and options available for individual toggling
    val toggleableOptions = listOf(
        FilterOption.PRONUNCIATION,
        FilterOption.EXPLANATION,
        FilterOption.SENTENCE,
        FilterOption.PROGRESS
    )
    // Define the special options
    val specialOptions = listOf(
        FilterOption.ALL,
        FilterOption.NONE
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        Text(
            "Filter Display",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        // Special options (All/None) - treated like radio buttons conceptually
        specialOptions.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.displayName) },
                onClick = {
                    // Selecting ALL or NONE sets the state exclusively
                    onOptionToggle(
                        option,
                        true
                    ) // Inform parent about the selection
                },
                trailingIcon = {
                    // Indicate selection (could use RadioButton or Checkbox if preferred)
                    if (selectedOptions.contains(option)) {
                        // Maybe add a checkmark or visual cue later if needed
                    }
                }
            )
        }

        // Divider? Optional

        // Toggleable options
        toggleableOptions.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            // Disable individual toggles if ALL or NONE is selected
            val enabled =
                !selectedOptions.contains(FilterOption.ALL) && !selectedOptions.contains(FilterOption.NONE)

            DropdownMenuItem(
                text = { Text(option.displayName) },
                onClick = {
                    if (enabled) { // Only toggle if not ALL/NONE
                        onOptionToggle(
                            option,
                            !isSelected
                        )
                    }
                },
                trailingIcon = {
                    Switch(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (enabled) { // Only toggle if not ALL/NONE
                                onOptionToggle(
                                    option,
                                    checked
                                )
                            }
                        },
                        enabled = enabled
                    )
                }
            )
        }
    }
}
