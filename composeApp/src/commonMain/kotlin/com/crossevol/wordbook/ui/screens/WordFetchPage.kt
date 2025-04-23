package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.components.sampleWordItem // For preview data
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun WordFetchPage(
    onBack: () -> Unit,
    // In a real app, you'd have a ViewModel or state holder
    // to manage the API call and results based on query, selection, and language.
    // For now, we use sample data.
    fetchedWord: WordItem? = sampleWordItem // Simulate fetched data
) {
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val dropdownOptions = listOf(
        "Option 1",
        "Option 2",
        "Option 3"
    ) // Sample options
    var selectedDropdownOption by remember { mutableStateOf(dropdownOptions[0]) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "EN",
        "JA",
        "ZH"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicit") }, // Title from image
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Blue from image
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp) // Padding for content
        ) {
            // Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("......") }, // Placeholder/Label from image
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Trigger search */ }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Selection
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDropdownOption,
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    label = { Text("Select...") }, // Label from image
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            Modifier.clickable { isDropdownExpanded = !isDropdownExpanded }
                        )
                    },
                    modifier = Modifier.fillMaxWidth() // Important for anchoring the dropdown
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    dropdownOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedDropdownOption = option
                                isDropdownExpanded = false
                                // TODO: Potentially trigger fetch/update based on selection
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Language Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface, // Background for tabs
                contentColor = MaterialTheme.colorScheme.primary // Color for selected tab indicator/text
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            // TODO: Trigger fetch/update based on language tab
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Fetched Content Area (similar to WordDetailPage content)
            if (fetchedWord != null) {
                // Pronunciation (like "[ Hello, world ]")
                Text(
                    text = fetchedWord.pronunciation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Explanation Label
                Text(
                    text = "Explanation", // "explanation" in design, capitalized here
                    style = MaterialTheme.typography.titleSmall, // Smaller title for label
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Explanation Text
                Text(
                    text = fetchedWord.explanation,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Sentences Label
                Text(
                    text = "Sentences",
                    style = MaterialTheme.typography.titleMedium, // Slightly larger title for section
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Sentences List (Reusing SentenceItem from WordDetailPage)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fetchedWord.sentences.forEach { sentence ->
                        // Using the SentenceItem from WordDetailPage - ensure it's accessible
                        // If WordDetailPage.kt's SentenceItem is private, extract it to a common components file.
                        // Assuming SentenceItem is public or in the same package for now.
                        SentenceItem(text = sentence) {
                            // Handle sentence click if needed
                            println("Sentence clicked: $sentence")
                        }
                    }
                }
            } else {
                // Placeholder for when no data is fetched yet
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Enter a query and search.")
                }
            }
        }
    }
}

@Preview
@Composable
fun WordFetchPagePreview() {
    MaterialTheme {
        Surface {
            WordFetchPage(onBack = {})
        }
    }
}

@Preview
@Composable
fun WordFetchPagePreview_NoData() {
    MaterialTheme {
        Surface {
            WordFetchPage(
                onBack = {},
                fetchedWord = null
            )
        }
    }
}
