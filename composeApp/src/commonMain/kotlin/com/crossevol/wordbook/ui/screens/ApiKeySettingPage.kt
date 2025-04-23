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
    onSaveChanges: (name: String, city: String, state: String) -> Unit // Example data to save
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Tab 1",
        "Tab 2",
        "Tab 3"
    )

    // State for the input fields (shared across tabs in this simple version)
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("") }
    val states = listOf(
        "California",
        "New York",
        "Texas",
        "Florida",
        "Other"
    ) // Example states
    var isStateDropdownExpanded by remember { mutableStateOf(false) }

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
            // Tab Row
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

            // Content Area (currently the same for all tabs based on design)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Alias") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // City TextField
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("ApiKey") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // State Dropdown
                ExposedDropdownMenuBox(
                    expanded = isStateDropdownExpanded,
                    onExpandedChange = { isStateDropdownExpanded = !isStateDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedState,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        label = { Text("Select State") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStateDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isStateDropdownExpanded,
                        onDismissRequest = { isStateDropdownExpanded = false }
                    ) {
                        states.forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state) },
                                onClick = {
                                    selectedState = state
                                    isStateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // Space before button

                // Save Button
                Button(
                    onClick = {
                        onSaveChanges(
                            name,
                            city,
                            selectedState
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
        ApiKeySettingPage(
            onNavigateBack = {},
            onSaveChanges = { _, _, _ -> })
    }
}
