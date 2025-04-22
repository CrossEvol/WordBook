package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // Added for button shape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource // Use painterResource for common resources
import org.jetbrains.compose.ui.tooling.preview.Preview
import wordbook.composeapp.generated.resources.Res // Import generated resources
import wordbook.composeapp.generated.resources.girl

/**
 * Page for editing user profile information.
 * Based on design/settings/EditProfilePage.png
 *
 * @param onNavigateBack Callback to navigate back.
 * @param onSaveChanges Callback when save button is clicked, passing the updated info.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun EditProfilePage(
    onNavigateBack: () -> Unit,
    onSaveChanges: (name: String, city: String, state: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
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
                title = { Text("Create your Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Match background
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Space from top bar

            // Avatar Image
            Image(
                painter = painterResource(Res.drawable.girl), // Load from common resources
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape), // Circular clip
                contentScale = ContentScale.Crop // Crop to fit circle
            )

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // City TextField
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Your City") },
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
                    onValueChange = {}, // Input is read-only, selection changes state
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

            // Bio TextField
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Your bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // Make bio field taller
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp)) // Space before button

            // Save Button
            Button(
                onClick = {
                    onSaveChanges(
                        name,
                        city,
                        selectedState,
                        bio
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

            Spacer(modifier = Modifier.height(16.dp)) // Space at the bottom
        }
    }
}

@Preview
@Composable
fun EditProfilePagePreview() {
    MaterialTheme {
        EditProfilePage(
            onNavigateBack = {},
            onSaveChanges = { _, _, _, _ -> })
    }
}
