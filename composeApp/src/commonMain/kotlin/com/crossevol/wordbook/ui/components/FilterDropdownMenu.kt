package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
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
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        // Special options (All/None) - treated like radio buttons conceptually
        specialOptions.forEach { option ->
            val isSelected = selectedOptions.contains(option)

            // Define custom colors for the 'None' switch when checked
            val switchColors = if (option == FilterOption.NONE) {
                SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colors.error,
                    checkedThumbColor = MaterialTheme.colors.onError // Use onError for contrast on the error track
                    // Unchecked colors will use defaults
                )
            } else {
                // Use default colors for other options like 'All'
                SwitchDefaults.colors()
            }

            DropdownMenuItem(
                onClick = {
                    // Selecting ALL or NONE sets the state exclusively
                    onOptionToggle(
                        option,
                        true
                    ) // Inform parent about the selection
                }
            ) { // Content is a RowScope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(option.displayName)
                    Switch(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            onOptionToggle(
                                option,
                                checked
                            )
                        },
                        enabled = true,
                        colors = switchColors // Apply the conditionally defined colors
                    )
                }
            }
        }

        // Divider? Optional

        // Toggleable options
        toggleableOptions.forEach { option ->
            val isSelected = selectedOptions.contains(option)

            DropdownMenuItem(
                onClick = {
                        onOptionToggle(
                            option,
                            !isSelected
                        )
                }
            ) { // Content is a RowScope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(option.displayName)
                    Switch(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                                onOptionToggle(
                                    option,
                                    checked
                                )
                        },
                        enabled = true
                    )
                }
            }
        }
    }
}
