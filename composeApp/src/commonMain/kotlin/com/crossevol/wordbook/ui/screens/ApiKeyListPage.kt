package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Dummy data for preview
val sampleApiKeyConfigs = listOf(
    ApiKeyConfig(
        1,
        "My Gemini Key",
        apiKey = "",
        "Google",
        "gemini-2.5-flash-preview-04-17"
    ),
    ApiKeyConfig(
        2,
        "My Claude Key",
        apiKey = "",
        "Anthropic",
        "claude-3-sonnet-20240229"
    ),
    ApiKeyConfig(
        3,
        "My OpenAI Key",
        apiKey = "",
        "OpenAI",
        "gpt-4o"
    ),
)

/**
 * Page for listing saved API Key configurations.
 * Based on design/settings/ApiKeySetting.png
 *
 * @param apiKeyConfigs The list of saved API key configurations.
 * @param onNavigateBack Callback to navigate back.
 * @param onAddApiKey Callback to navigate to the Add API Key page.
 * @param onEditApiKey Callback when an API key item's Edit button is clicked.
 * @param onDeleteApiKey Callback when an API key item's Delete button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyListPage(
    apiKeyConfigs: List<ApiKeyConfig> = sampleApiKeyConfigs, // Use dummy data for preview
    onNavigateBack: () -> Unit,
    onAddApiKey: () -> Unit,
    onEditApiKey: (ApiKeyConfig) -> Unit,
    onDeleteApiKey: (ApiKeyConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key Settings") }, // Title from design
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            items(apiKeyConfigs) { config ->
                ApiKeyItem(
                    config = config,
                    onEditClick = { onEditApiKey(config) },
                    onDeleteClick = { onDeleteApiKey(config) }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer // Use secondaryContainer for card background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, // Adjust font size
                    color = MaterialTheme.colorScheme.onSecondaryContainer // Text color
                )
                Text(
                    text = config.model,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp, // Adjust font size
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) // Slightly faded color
                )
            }

            // Action Buttons (Edit and Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp), // Button size
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Primary color for Edit button
                        contentColor = MaterialTheme.colorScheme.onPrimary // Icon color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit API Key",
                        modifier = Modifier.size(24.dp) // Icon size
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp), // Button size
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error, // Error color for Delete button
                        contentColor = MaterialTheme.colorScheme.onError // Icon color
                    )
                ) {
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


@Preview
@Composable
fun ApiKeyListPagePreview() {
    MaterialTheme {
        ApiKeyListPage(
            onNavigateBack = {},
            onAddApiKey = {},
            onEditApiKey = {},
            onDeleteApiKey = {}
        )
    }
}
