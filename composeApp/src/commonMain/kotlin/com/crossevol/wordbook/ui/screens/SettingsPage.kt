package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossevol.wordbook.data.NotificationFrequency // Import enum
import com.crossevol.wordbook.data.SettingsRepository // Import SettingsRepository
import com.crossevol.wordbook.getDefaultDocumentsPath // Import the new function
import com.crossevol.wordbook.getPlatform
import com.crossevol.wordbook.ui.svgicons.MyIconPack
import com.crossevol.wordbook.ui.svgicons.myiconpack.Description
import com.crossevol.wordbook.ui.svgicons.myiconpack.DocumentScanner
import com.crossevol.wordbook.ui.svgicons.myiconpack.SendAndArchive
import com.crossevol.wordbook.ui.svgicons.myiconpack.Unarchive
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.crossevol.wordbook.ui.components.NotificationSettingsDialog // Import the dialog
import com.darkrockstudios.libraries.mpfilepicker.FilePicker // Import FilePicker
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import wordbook.composeapp.generated.resources.Res
import wordbook.composeapp.generated.resources.girl

/**
 * Settings Page composable based on the design image SettingsPage.png.
 *
 * @param settingsRepository Repository for accessing and saving settings. Required.
 * @param username The username to display.
 * @param email The email address to display.
 * @param onNavigateBack Callback to navigate back (e.g., to HomePage).
 * @param onEditProfile Callback when "Edit Profile" is clicked.
 * @param onChangeApiKey Callback when "Change ApiKey" is clicked.
 * @param onExport Callback when export is confirmed, providing path and format.
 * @param onImport Callback when import is confirmed, providing file path and format.
 * @param onIntroduction Callback when "Introduction" is clicked.
 * @param onTermsOfService Callback when "Terms of Services" is clicked.
 * @param onLogout Callback when "Log Out" is clicked.
 */
@Composable
fun SettingsPage(
    username: String = "[Username]", // Default values for preview
    email: String = "[Email_Address]", // Default values for preview
    settingsRepository: SettingsRepository?, // Make nullable for preview, but required in practice
    onNavigateBack: () -> Unit, // Required for navigation
    onEditProfile: () -> Unit = {},
    onChangeApiKey: () -> Unit = {}, // This will now navigate to ApiKeyListPage
    onExport: (path: String, format: String) -> Unit = { _, _ -> }, // Updated signature
    onImport: (path: String, format: String) -> Unit = { _, _ -> }, // Updated signature for import
    onIntroduction: () -> Unit = {},
    onTermsOfService: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // State for showing the export dialog
    var showExportDialog by remember { mutableStateOf(false) }
    // State for showing the import dialog
    var showImportDialog by remember { mutableStateOf(false) } // State for import dialog
    // State for showing the notification settings dialog
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Define the primary color from the image header (approximate)
    val headerColor = Color(0xFF4A148C) // Deep Purple / Indigo

    // Show the export dialog if state is true
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { path, format ->
                // Handle export with the selected path and format
                onExport(path, format) // Call the provided onExport callback
                showExportDialog = false
            }
        )
    }

    // Show the import dialog if state is true
    if (showImportDialog) {
        ImportDialog( // Call the new ImportDialog
            onDismiss = { showImportDialog = false },
            onConfirm = { path, format ->
                // Handle import with the selected file path and format
                onImport(path, format) // Call the provided onImport callback
                showImportDialog = false
            }
        )
    }

    // Show the notification settings dialog if state is true
    if (showNotificationDialog && settingsRepository != null) { // Ensure repository is not null
        // Load initial settings from the repository
        val initialPermission = remember { settingsRepository.getNotificationPermissionEnabled() }
        val initialFrequenciesEnabled = remember {
            NotificationFrequency.entries.associateWith { freq ->
                settingsRepository.isNotificationFrequencyEnabled(freq)
            }
        }
        val initialStartTimes = remember {
            NotificationFrequency.entries.associateWith { freq ->
                settingsRepository.getNotificationFrequencyStartTime(freq)
            }
        }

        NotificationSettingsDialog(
            initialPermissionEnabled = initialPermission,
            initialEnabledFrequencies = initialFrequenciesEnabled,
            initialStartTimes = initialStartTimes,
            onDismissRequest = { showNotificationDialog = false },
            onConfirm = { permissionEnabled, enabledFrequencies, startTimes ->
                // Save the updated settings back to the repository
                settingsRepository.setNotificationPermissionEnabled(permissionEnabled)
                enabledFrequencies.forEach { (freq, enabled) -> settingsRepository.setNotificationFrequencyEnabled(freq, enabled) }
                startTimes.forEach { (freq, time) -> settingsRepository.setNotificationFrequencyStartTime(freq, time) }
                showNotificationDialog = false // Close the dialog
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background) // Use theme background
    ) {
        // Header Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // Adjust height as needed
            color = headerColor,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp) // Rounded bottom corners
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Image
                Image(
                    painter = painterResource(Res.drawable.girl), // Load from common resources
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(80.dp) // Keep original size
                        .clip(RoundedCornerShape(12.dp)), // Keep original shape
                    contentScale = ContentScale.Crop // Crop to fit bounds
                )
                Spacer(modifier = Modifier.width(16.dp))
                // User Info
                Column {
                    Text(
                        text = username,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        color = Color.White.copy(alpha = 0.8f), // Slightly transparent
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Settings Items Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
        ) {
            SettingsItem(
                icon = Icons.Filled.AccountBox, // Correct Icon
                text = "Edit Profile",
                onClick = onEditProfile
            )
            SettingsItem(
                icon = Icons.Filled.Lock, // Correct Icon
                text = "Change ApiKey",
                onClick = onChangeApiKey // This now navigates to ApiKeyListPage
            )
            SettingsItem(
                icon = MyIconPack.SendAndArchive, // Icon for Export
                text = "Export",
                onClick = { showExportDialog = true } // Show export dialog
            )
            SettingsItem(
                icon = MyIconPack.Unarchive, // Icon for Import
                text = "Import",
                onClick = { showImportDialog = true } // Show import dialog
            )
            SettingsItem(
                icon = Icons.Filled.Notifications,
                text = "Notification Settings",
                onClick = {
                    // Show the notification dialog when clicked
                    showNotificationDialog = true
                }
            )
            SettingsItem(
                icon = Icons.Filled.Info,
                text = "Introduction",
                onClick = onIntroduction
            )
            SettingsItem(
                icon = MyIconPack.Description, // Correct Icon
                text = "Terms of Services",
                onClick = onTermsOfService
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Push logout button to bottom

        // Log Out Button
        Button(
            onClick = onNavigateBack, // Changed to onNavigateBack as per button text
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp) // More padding around button
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.onPrimary, // Light background for button
                contentColor = MaterialTheme.colors.onSurface // Text color
            ),
            elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp, // Correct Icon
                contentDescription = null, // Button text is descriptive
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Back Home", fontSize = 16.sp)
        }
    }
}

/**
 * Reusable composable for each setting item row.
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.colors.surface, // White/Surface background
        elevation = 1.dp // Subtle shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Text describes the action
                tint = MaterialTheme.colors.onSurface // Icon color
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface // Text color
            )
        }
    }
}


@Preview
@Composable
fun SettingsPagePreview() {
    MaterialTheme {
        SettingsPage(
            settingsRepository = null, // No repository in preview
            onNavigateBack = {} // Provide dummy lambda for preview
        )
    }
}

/**
 * Dialog for exporting data with path selection and format options.
 */
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onConfirm: (path: String, format: String) -> Unit
) {
    // State for showing the directory picker
    var showDirectoryPicker by remember { mutableStateOf(false) } // State to control picker visibility

    // Get the default path *before* initializing the state
    val defaultPath = getDefaultDocumentsPath()

    // State for the text field and selected format
    // Initialize path with the default documents path obtained above
    var path by remember { mutableStateOf(defaultPath) }
    var selectedFormat by remember { mutableStateOf("JSON") }
    
    // State for showing a warning about the selected path
    var showPathWarning by remember { mutableStateOf(false) }
    var pathWarningMessage by remember { mutableStateOf("") }

    // List of available export formats
    val exportFormats = listOf("JSON", "CSV")

    // Directory Picker Composable
    DirectoryPicker(show = showDirectoryPicker) { selectedPath ->
        showDirectoryPicker = false // Hide the picker when a selection is made or cancelled
        if (selectedPath != null) {
            // Check if the path is a content URI or a file path
            if (selectedPath.startsWith("content://")) {
                // Content URI is good for Android
                path = selectedPath
                showPathWarning = false
            } else {
                // For file paths, we'll use them but show a warning on Android
                path = selectedPath
                val platform = getPlatform().name
                if (platform.startsWith("Android")) {
                    pathWarningMessage = "Using app-specific storage. Files will be saved to the app's private documents folder."
                    showPathWarning = true
                } else {
                    showPathWarning = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Data") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Path selection text field (now read-only, triggers picker)
                OutlinedTextField(
                    value = path,
                    onValueChange = { /* Read-only, value is set by picker */ },
                    readOnly = true, // Make the text field read-only
                    label = { Text("Export Path") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            showDirectoryPicker = true // Show the directory picker
                        }) {
                            Icon(
                                imageVector = MyIconPack.DocumentScanner,
                                contentDescription = "Select Folder"
                            )
                        }
                    },
                    singleLine = true
                )
                
                // Show warning message if needed
                if (showPathWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pathWarningMessage,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Format selection radio group
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .fillMaxWidth()
                ) {
                    exportFormats.forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (format == selectedFormat),
                                    onClick = { selectedFormat = format },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (format == selectedFormat),
                                onClick = null // null because we're handling clicks on the row
                            )
                            Text(
                                text = format,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(path, selectedFormat) } // Use the path state updated by the picker
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for importing data with file selection and format options.
 */
@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (path: String, format: String) -> Unit
) {
    // State for showing the file picker
    var showFilePicker by remember { mutableStateOf(false) } // State to control picker visibility

    // Get the default path *before* initializing the state
    val defaultPath = getDefaultDocumentsPath() // Get the default path

    // State for the text field and selected format
    // Initialize path with the default documents path obtained above
    var path by remember { mutableStateOf(defaultPath) } // Initialize path with defaultPath
    var selectedFormat by remember { mutableStateOf("JSON") } // Default format

    // List of available import formats and their corresponding file extensions
    val importFormats = listOf("JSON", "CSV", "TXT")
    val fileExtensions = listOf("json", "csv", "txt") // Extensions for the file picker filter

    // File Picker Composable
    FilePicker(
        show = showFilePicker,
        initialDirectory = defaultPath, // <-- Added initialDirectory here
        fileExtensions = fileExtensions
    ) { platformFile ->
        showFilePicker = false // Hide the picker when a selection is made or cancelled
        if (platformFile != null) {
            // Update the path state with the selected file path
            path = platformFile.path
            
            // Try to infer format from file extension
            val extension = platformFile.path.substringAfterLast('.', "").lowercase()
            when (extension) {
                "json" -> selectedFormat = "JSON"
                "csv" -> selectedFormat = "CSV"
                "txt" -> selectedFormat = "TXT"
                // Default format remains unchanged if extension doesn't match
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Data") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // File selection text field (read-only, triggers picker)
                OutlinedTextField(
                    value = path, // Use the path state
                    onValueChange = { /* Read-only, value is set by picker */ },
                    readOnly = true, // Make the text field read-only
                    label = { Text("Import File") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            showFilePicker = true // Show the file picker
                        }) {
                            Icon(
                                imageVector = MyIconPack.DocumentScanner, // Using the same icon for file/folder selection
                                contentDescription = "Select File"
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Format selection radio group
                Text(
                    text = "Import Format",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .fillMaxWidth()
                ) {
                    importFormats.forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (format == selectedFormat),
                                    onClick = { selectedFormat = format },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (format == selectedFormat),
                                onClick = null // null because we're handling clicks on the row
                            )
                            Text(
                                text = format,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                // Enable confirm button only if a file path has been selected
                onClick = { onConfirm(path, selectedFormat) },
                enabled = path.isNotBlank() // Disable button if no file is selected
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
