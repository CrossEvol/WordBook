package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossevol.wordbook.data.NotificationFrequency
import com.crossevol.wordbook.PlatformTimePicker // Import the expect composable
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Locale // Import Locale for formatting

private val logger = KotlinLogging.logger {}

// Helper function to parse "HH:mm" string
private fun parseTime(time: String): Pair<Int, Int> {
    return try {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    } catch (e: Exception) {
        logger.warn(e) { "Failed to parse time string: $time, defaulting to 00:00" }
        Pair(0, 0) // Default to 00:00 on error
    }
}

// Helper function to format hour/minute to "HH:mm" string
private fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.US, "%02d:%02d", hour, minute)
}

// Data class to hold the state within the dialog
private data class DialogState(
    val permissionEnabled: Boolean,
    val enabledFrequencies: Map<NotificationFrequency, Boolean>,
    val startTimes: Map<NotificationFrequency, String>
)

/**
 * A dialog for configuring notification settings based on the provided design.
 *
 * @param initialPermissionEnabled The initial state for the permission toggle.
 * @param initialEnabledFrequencies A map indicating which frequencies are initially enabled.
 * @param initialStartTimes A map of initial start times (HH:mm) for each frequency.
 * @param onDismissRequest Called when the dialog should be dismissed without saving changes.
 * @param onConfirm Called when the user confirms the settings. Passes the updated state.
 */
@Composable
fun NotificationSettingsDialog(
    initialPermissionEnabled: Boolean,
    initialEnabledFrequencies: Map<NotificationFrequency, Boolean>,
    initialStartTimes: Map<NotificationFrequency, String>,
    onDismissRequest: () -> Unit,
    onConfirm: (
        permissionEnabled: Boolean,
        enabledFrequencies: Map<NotificationFrequency, Boolean>,
        startTimes: Map<NotificationFrequency, String>
    ) -> Unit
) {
    // Internal state for the dialog, initialized from parameters
    var dialogState by remember {
        mutableStateOf(
            DialogState(
                permissionEnabled = initialPermissionEnabled,
                enabledFrequencies = initialEnabledFrequencies,
                startTimes = initialStartTimes
            )
        )
    }

    // State for managing the time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }
    // State to track which frequency's time is being edited
    var editingFrequency by remember { mutableStateOf<NotificationFrequency?>(null) }

    val cyanColor = Color(0xFF06D5CD) // Color from the design

    // --- Time Picker ---
    // Only compose the PlatformTimePicker when needed
    editingFrequency?.let { freqToEdit ->
        val (initialHour, initialMinute) = parseTime(dialogState.startTimes[freqToEdit] ?: "00:00")
        PlatformTimePicker(
            show = showTimePicker,
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = {
                showTimePicker = false
                editingFrequency = null // Clear editing state on dismiss
            },
            onTimeSelected = { hour, minute ->
                val updatedTimes = dialogState.startTimes.toMutableMap()
                updatedTimes[freqToEdit] = formatTime(hour, minute) // Use helper to format
                dialogState = dialogState.copy(startTimes = updatedTimes)
                showTimePicker = false
                editingFrequency = null // Clear editing state after selection
            }
        )
    }
    // --- End Time Picker ---


    AlertDialog(
        onDismissRequest = onDismissRequest,
        backgroundColor = MaterialTheme.colors.surface, // Use theme surface color
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notification Settings Icon",
                    tint = MaterialTheme.colors.onSurface,
                    modifier = Modifier.size(44.dp).padding(bottom = 16.dp)
                )
                Text(
                    "Notification Setting",
                    style = MaterialTheme.typography.h3.copy(fontSize = 24.sp), // Adjusted size
                    color = MaterialTheme.colors.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // --- Permission Section ---
                Text(
                    "Permission",
                    style = MaterialTheme.typography.h1.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Use Column instead of Row for vertical layout
                Column(Modifier.selectableGroup()) {
                    RadioButtonOption(
                        text = "Permit",
                        selected = dialogState.permissionEnabled,
                        onClick = {
                            dialogState = dialogState.copy(permissionEnabled = true)
                        },
                        enabledColor = cyanColor // Use cyan for selected radio
                    )
                    // Removed Spacer(modifier = Modifier.width(16.dp))
                    RadioButtonOption(
                        text = "Forbid",
                        selected = !dialogState.permissionEnabled,
                        onClick = {
                            dialogState = dialogState.copy(permissionEnabled = false)
                        },
                        enabledColor = cyanColor // Use cyan for selected radio
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Frequency Section ---
                Text(
                    "Frequency",
                    style = MaterialTheme.typography.h1.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    NotificationFrequency.entries.forEach { freq ->
                        FrequencyItem(
                            text = freq.displayName,
                            time = dialogState.startTimes[freq] ?: "00:00",
                            isChecked = dialogState.enabledFrequencies[freq] ?: false,
                            onCheckedChange = { isChecked ->
                                val updatedFrequencies = dialogState.enabledFrequencies.toMutableMap()
                                updatedFrequencies[freq] = isChecked
                                dialogState = dialogState.copy(enabledFrequencies = updatedFrequencies)
                            },
                            onTimeClick = {
                                // --- Placeholder for Time Picker ---
                                // In a real app, you'd show a TimePickerDialog here.
                                // For now, we just log it. You could update the state
                                // --- Show the Time Picker ---
                                editingFrequency = freq // Set which frequency we are editing
                                showTimePicker = true // Trigger the picker display
                                // --- End Show Time Picker ---
                            },
                            // Use the color from the enum entry directly
                            enabledColor = freq.color
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Space between frequency items
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        dialogState.permissionEnabled,
                        dialogState.enabledFrequencies,
                        dialogState.startTimes
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = cyanColor),
                shape = CircleShape // Rounded button
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp) // Padding around the dialog content
    )
}

@Composable
private fun RadioButtonOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabledColor: Color = MaterialTheme.colors.primary // Default to primary
) {
    Row(
        Modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp), // Padding for touch target
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // Handled by Row's selectable
            colors = RadioButtonDefaults.colors(
                selectedColor = enabledColor, // Use the passed color when selected
                unselectedColor = MaterialTheme.colors.onSurface
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onSurface)
    }
}

@Composable
private fun FrequencyItem(
    text: String,
    time: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    enabledColor: Color // Color for checkbox and circle
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle indicator
        Box(
            modifier = Modifier
                .size(24.dp) // Smaller circle
                .clip(CircleShape)
                .background(enabledColor) // Use the cyan color
        )
        Spacer(modifier = Modifier.width(16.dp))
        // Keep text aligned to the start
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.weight(1f) // Allow text to take available space pushing time/checkbox right
        )
        // Group Time and Checkbox together at the end
        Text(
            text = time,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small) // Add clip for visual feedback on click
                .clickable(onClick = onTimeClick) // Make time clickable
                .padding(horizontal = 8.dp, vertical = 4.dp) // Padding for click area
        )
        Spacer(modifier = Modifier.width(8.dp)) // Space between time and checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = enabledColor, // Use cyan color when checked
                uncheckedColor = MaterialTheme.colors.onSurface
            )
        )
    }
}
