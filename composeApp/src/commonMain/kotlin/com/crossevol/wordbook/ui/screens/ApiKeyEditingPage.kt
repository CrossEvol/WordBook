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
import com.crossevol.wordbook.ui.viewmodel.ApiKeyViewModel // Import ViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

// Define data structure for LLM providers and models
data class LLMProvider(
    val name: String,
    val models: List<String>
)

val llmProviders = listOf(
    LLMProvider(
        "Google", // Includes Gemma and the detailed Gemini list provided
        listOf(
            // Gemma Models (from previous step, relevant by April 2024)
            "gemma-7b-it",
            "gemma-2b-it",

            // Detailed Gemini Models (as provided by user, includes previews/exp/future)
            "gemini-1.0-pro",
            "gemini-1.0-pro-001",
            "gemini-1.0-pro-002",
            "gemini-1.0-pro-vision",       // Vision model
            "gemini-1.0-pro-vision-001",   // Vision model specific version
            "gemini-1.0-ultra",            // Original Ultra
            "gemini-1.0-ultra-001",        // Original Ultra specific version
            "gemini-1.5-flash",            // Base 1.5 Flash
            "gemini-1.5-flash-001",
            "gemini-1.5-flash-002",
            "gemini-1.5-flash-exp-0827",   // Experimental
            "gemini-1.5-flash-preview-051",// Preview (May 2024) - Note: Check if '-0514' intended?
            "gemini-1.5-pro",              // Base 1.5 Pro
            "gemini-1.5-pro-001",
            "gemini-1.5-pro-002",
            "gemini-1.5-pro-preview-0215", // Preview (Feb 2024)
            "gemini-1.5-pro-preview-0409", // Preview (Apr 2024)
            "gemini-1.5-pro-preview-0514", // Preview (May 2024)
            // Note: Version 2.0/2.5 models are likely internal/experimental/unreleased as of Apr 2024
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-2.0-flash-exp",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash-lite-001",
            "gemini-2.0-flash-thinking-ex", // Experimental
            "gemini-2.0-pro-exp-02-05",     // Experimental
            "gemini-2.5-flash-preview-04-17", // Preview - Note: Trailing '-'?
            "gemini-2.5-pro-exp-03-25",     // Experimental
            "gemini-2.5-pro-preview-03-25"  // Preview
        )
    ),
    LLMProvider(
        "Anthropic", // Common name for the company behind Claude
        listOf(
            "claude-3-opus-20240229",
            "claude-3-sonnet-20240229",
            "claude-3-haiku-20240229",
            "claude-2.1",
            "claude-instant-1.2"
        )
    ),
    LLMProvider(
        "OpenAI",
        listOf(
            "gpt-4o", // Announced May 2024, kept from original request context
            "gpt-4-turbo", // Often alias for latest, e.g., gpt-4-turbo-2024-04-09
            "gpt-4-turbo-preview", // Older preview alias
            "gpt-4",
            "gpt-3.5-turbo", // Often alias for latest, e.g., gpt-3.5-turbo-0125
            "gpt-3.5-turbo-instruct"
        )
    ),
    LLMProvider(
        "Mistral AI",
        listOf(
            "mistral-large-latest", // Feb 2024
            "mistral-small-latest", // Feb 2024
            "mixtral-8x22b-instruct-v0.1", // Apr 2024 (open model)
            "mixtral-8x7b-instruct-v0.1", // Dec 2023
            "mistral-7b-instruct-v0.2"
        )
    ),
    LLMProvider(
        "Meta",
        listOf(
            "llama-3-70b-instruct", // Released April 18, 2024
            "llama-3-8b-instruct",  // Released April 18, 2024
            "llama-2-70b-chat",
            "llama-2-13b-chat",
            "llama-2-7b-chat"
        )
    ),
    LLMProvider(
        "Cohere",
        listOf(
            "command-r-plus", // Released April 2024
            "command-r",      // Released March 2024
            "command",
            "command-light"
        )
    ),
    LLMProvider(
        "DeepSeek",
        listOf(
            "deepseek-coder-33b-instruct",
            "deepseek-coder-6.7b-instruct",
            "deepseek-coder-1.3b-instruct",
            "deepseek-llm-67b-chat",
            "deepseek-llm-7b-chat"
        )
    ),
    LLMProvider(
        "AI21 Labs",
        listOf(
            "jamba-instruct", // March 2024
            "j2-ultra",
            "j2-mid",
            "j2-light"
        )
    ),
    LLMProvider(
        "Perplexity", // Models often fine-tuned/hosted versions
        listOf(
            "llama-3-sonar-large-32k-online", // Based on Llama 3 (April 2024)
            "llama-3-sonar-large-32k-chat",
            "llama-3-sonar-small-32k-online", // Based on Llama 3 (April 2024)
            "llama-3-sonar-small-32k-chat",
            "pplx-70b-online", // Older models, likely Llama 2 based
            "pplx-7b-online"   // Older models, likely Mistral based
        )
    )
    // Add more providers/models as needed
)


/**
 * Page for adding or editing an API Key configuration.
 * Based on design/settings/ApiKeySetting.png (conceptually, though the image shows a list)
 *
 * @param viewModel The ViewModel handling the save operation.
 * @param config The ApiKeyConfig to edit, or null for adding a new one.
 * @param onNavigateBack Callback to navigate back.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun ApiKeyEditingPage(
    viewModel: ApiKeyViewModel, // Receive ViewModel
    config: ApiKeyConfig? = null, // Optional config for editing
    onNavigateBack: () -> Unit,
    // onSaveChanges callback is now handled internally by calling the ViewModel
) {
    // State for the input fields, initialized with config data if editing
    var alias by remember { mutableStateOf(config?.alias ?: "") }
    // API Key is typically not pre-filled for security, but we might need it for update
    // Let's initialize it with the config's key if editing, but advise caution.
    // A more secure approach would be to require re-entry on edit.
    var apiKey by remember { mutableStateOf(config?.apiKey ?: "") }
    var selectedProviderName by remember { mutableStateOf(config?.provider ?: "") }
    var selectedModelName by remember { mutableStateOf(config?.model ?: "") }

    // State for the LLM selection dropdowns
    var isProviderDropdownExpanded by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    // Get models for the currently selected provider
    val availableModels =
        llmProviders.find { it.name == selectedProviderName }?.models ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (config == null) "Add API Key" else "Edit API Key") }, // Title changes based on mode
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
                    placeholder = { Text("Enter API Key (required)") }, // Hint for key input
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
                        // Create the ApiKeyConfig object
                        val configToSave = ApiKeyConfig(
                            id = config?.id ?: 0L, // Use existing ID if editing, 0L for new
                            alias = alias,
                            apiKey = apiKey,
                            provider = selectedProviderName,
                            model = selectedModelName
                        )
                        // Call the ViewModel's save function
                        viewModel.saveApiKeyConfig(configToSave)
                        // Navigate back after saving
                        onNavigateBack()
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

