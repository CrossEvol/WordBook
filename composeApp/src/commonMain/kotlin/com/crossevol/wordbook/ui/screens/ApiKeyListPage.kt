package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.* // Keep Material 2 import
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossevol.wordbook.ui.viewmodel.ApiKeyViewModel // Import ViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

// Data class to represent a saved API Key configuration
data class ApiKeyConfig(
    val id: Long = 0, // Add an ID for potential database operations
    val alias: String,
    val apiKey: String,
    val provider: String,
    val model: String
    // Note: API Key itself is not stored here for security/simplicity in UI model
)

// Dummy data for preview - REMOVED, data comes from DB now

/**
 * Page for listing saved API Key configurations.
 * Based on design/settings/ApiKeySetting.png
 *
 * @param viewModel The ViewModel providing the list of API keys and handling actions.
 * @param onNavigateBack Callback to navigate back.
 * @param onAddApiKey Callback to navigate to the Add API Key page.
 * @param onEditApiKey Callback when an API key item's Edit button is clicked.
 */
@Composable
fun ApiKeyListPage(
    viewModel: ApiKeyViewModel, // Receive ViewModel
    onNavigateBack: () -> Unit,
    onAddApiKey: () -> Unit,
    onEditApiKey: (ApiKeyConfig) -> Unit,
    // onDeleteApiKey is now handled by the ViewModel directly from the item composable
) {
    // Collect the list of API keys from the ViewModel's StateFlow
    val apiKeyConfigs by viewModel.apiKeyConfigs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key Settings") }, // Title from design
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                // Using Material 2 TopAppBar parameters
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddApiKey) {
                Icon(
                    Icons.Filled.Add,
                    "Add new API Key"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ), // Add padding around the list
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between list items
        ) {
            items(apiKeyConfigs, key = { it.id }) { config -> // Add key for better list performance
                ApiKeyItem(
                    config = config,
                    onEditClick = { onEditApiKey(config) },
                    onDeleteClick = { viewModel.deleteApiKeyConfig(config) } // Call ViewModel delete
                )
            }
        }
    }
}

/**
 * Composable for a single API Key list item.
 */
@Composable
fun ApiKeyItem(
    config: ApiKeyConfig,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }, // Make the whole card clickable to edit
        shape = RoundedCornerShape(10.dp),
        // Using Material 2 Card parameters
        backgroundColor = MaterialTheme.colors.secondary, // Use secondary for card background in M2
        elevation = 4.dp // Use elevation parameter directly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Space out content and buttons
        ) {
            // Text Content (Alias and Model)
            Column(
                modifier = Modifier.weight(1f)
                    .padding(end = 8.dp) // Allow text to take space, add end padding
            ) {
                Text(
                    text = config.alias,
                    style = MaterialTheme.typography.h6, // Adjusted typography for better M2 fit
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, // Adjust font size
                    color = MaterialTheme.colors.onSecondary // Use M2 color
                )
                Text(
                    text = config.model,
                    style = MaterialTheme.typography.body2, // Adjusted typography for better M2 fit
                    fontSize = 14.sp, // Adjust font size
                    color = MaterialTheme.colors.onSecondary.copy(alpha = 0.8f) // Use M2 color
                )
            }

            // Action Buttons (Edit and Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
            ) {
                // Edit Button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp), // Button size
                    // Removed colors parameter
                ) {
                    // Provide LocalContentColor for the Icon
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit API Key",
                            modifier = Modifier.size(24.dp) // Icon size
                        )
                    }
                }
                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp), // Button size
                    // Removed colors parameter
                ) {
                    // Provide LocalContentColor for the Icon
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.error) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete API Key",
                            modifier = Modifier.size(24.dp) // Icon size
                        )
                    }
                }
            }
        }
    }
}

// Added a preview using the MockViewModel
@Preview
@Composable
fun PreviewApiKeyListPage() {
    // Use a mock ViewModel for preview
    val mockViewModel = ApiKeyViewModel(MockApiKeyConfigRepository())
    // Load some dummy data into the mock ViewModel's state
    mockViewModel.loadApiKeyConfigs()

    // Wrap in a MaterialTheme for preview
    MaterialTheme {
        ApiKeyListPage(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onAddApiKey = {},
            onEditApiKey = {}
        )
    }
}

