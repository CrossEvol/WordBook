package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// Define data structure for LLM providers and models
data class LLMProvider(
    val name: String,
    val models: List<String>
)

val llmProviders = listOf(
    LLMProvider("Gemini", listOf("gemini-pro", "gemini-1.5-flash-latest", "gemini-1.5-pro-latest")),
    LLMProvider("Claude", listOf("claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240229")),
    LLMProvider("OpenAI", listOf("gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo")),
    LLMProvider("DeepSeek", listOf("deepseek-coder", "deepseek-chat")),
    // Add more providers/models as needed
)


/**
 * Page for setting API Key, potentially with tabs.
 * Based on design/settings/ApiKeySetting.png
 *
 * @param onNavigateBack Callback to navigate back.
 * @param onSaveChanges Callback when save button is clicked.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun ApiKeySettingPage(
    onNavigateBack: () -> Unit,
    // Updated signature to pass alias, apiKey, provider, and model
    onSaveChanges: (alias: String, apiKey: String, provider: String, model: String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Tab 1",
        "Tab 2",
        "Tab 3"
    )

    // State for the input fields
    var alias by remember { mutableStateOf("") } // Renamed from 'name' to 'alias' for clarity
    var apiKey by remember { mutableStateOf("") } // Renamed from 'city' to 'apiKey' for clarity

    // State for the LLM selection dropdowns
    var selectedProviderName by remember { mutableStateOf("") }
    var selectedModelName by remember { mutableStateOf("") }
    var isProviderDropdownExpanded by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    // Get models for the currently selected provider
    val availableModels = llmProviders.find { it.name == selectedProviderName }?.models ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set your ApiKey") }, // Title from design
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Make content scrollable
        ) {
            // Tab Row (kept as is, though content is the same)
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) { // Use PrimaryTabRow for Material 3 style
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                        // Customize selected/unselected colors if needed
                    )
                }
            }

            // Content Area (API Key and LLM Selection)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Alias TextField
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // ApiKey TextField
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("ApiKey") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // LLM Provider Dropdown (First Level)
                ExposedDropdownMenuBox(
                    expanded = isProviderDropdownExpanded,
                    onExpandedChange = { isProviderDropdownExpanded = !isProviderDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProviderName,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        label = { Text("Select Provider") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProviderDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isProviderDropdownExpanded,
                        onDismissRequest = { isProviderDropdownExpanded = false }
                    ) {
                        llmProviders.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.name) },
                                onClick = {
                                    selectedProviderName = provider.name
                                    selectedModelName = "" // Reset model when provider changes
                                    isProviderDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // LLM Model Dropdown (Second Level)
                // Only enable if a provider is selected
                ExposedDropdownMenuBox(
                    expanded = isModelDropdownExpanded,
                    onExpandedChange = {
                        if (selectedProviderName.isNotEmpty()) { // Only expand if provider is selected
                            isModelDropdownExpanded = !isModelDropdownExpanded
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedModelName,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        label = { Text("Select Model") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = selectedProviderName.isNotEmpty() // Disable if no provider selected
                    )
                    ExposedDropdownMenu(
                        expanded = isModelDropdownExpanded,
                        onDismissRequest = { isModelDropdownExpanded = false }
                    ) {
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    selectedModelName = model
                                    isModelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp)) // Space before button

                // Save Button
                Button(
                    onClick = {
                        // Pass all relevant data to the updated callback
                        onSaveChanges(
                            alias,
                            apiKey,
                            selectedProviderName,
                            selectedModelName
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)) // Purple button
                ) {
                    Text(
                        "Save Changes",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ApiKeySettingPagePreview() {
    MaterialTheme {
        // Update preview call to match the new signature
        ApiKeySettingPage(
            onNavigateBack = {},
            onSaveChanges = { _, _, _, _ -> } // Provide a lambda matching the new signature
        )
    }
}
