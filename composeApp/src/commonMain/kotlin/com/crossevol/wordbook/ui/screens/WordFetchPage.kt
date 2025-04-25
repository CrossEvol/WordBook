package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh // Use Refresh icon for reset
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.data.api.WordFetchResultJson // Import the data class
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel // Import ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {} // Add logger instance

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun WordFetchPage(
    viewModel: WordFetchViewModel, // Accept ViewModel as parameter
    onBack: () -> Unit,
) {
    // Observe state from ViewModel
    val searchQuery = viewModel.searchQuery
    val selectedModel = viewModel.selectedModel
    val selectedLanguageTabIndex = viewModel.selectedLanguageTabIndex
    val isLoading = viewModel.isLoading
    val fetchedResult = viewModel.fetchedResult
    val errorMessage = viewModel.errorMessage

    // State for dropdown menu
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Determine if fetched content should be shown (based on whether result is available)
    val showFetchedContent = fetchedResult != null

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
                actions = {
                    // Reset button - only show if content is currently shown or loading/error
                    if (showFetchedContent || isLoading || errorMessage != null) {
                        IconButton(onClick = { viewModel.resetPage() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset"
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ) // Padding for content
                .verticalScroll(rememberScrollState()) // Make content scrollable
        ) {
            // Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) }, // Update ViewModel state
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Input Word ...") }, // Placeholder/Label from image
                trailingIcon = {
                    IconButton(onClick = { viewModel.fetchWord() }) { // Trigger fetch on search icon click too? Or just button? Let's use button for now.
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                singleLine = true,
                enabled = !isLoading // Disable input while loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Selection
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    label = { Text("Select Model") }, // Label from image
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            Modifier.clickable { isDropdownExpanded = !isDropdownExpanded }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(), // Important for anchoring the dropdown
                    enabled = !isLoading // Disable while loading
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    viewModel.modelOptions.forEach { option -> // Use options from ViewModel
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onModelSelect(option) // Update ViewModel state
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Translate Button
            Button(
                onClick = { viewModel.fetchWord() }, // Trigger fetch via ViewModel
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp), // Height from Flutter code
                shape = MaterialTheme.shapes.small, // Use small shape for 8dp border radius
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary // Primary color for button
                ),
                enabled = !isLoading // Disable button while loading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Translate",
                        color = MaterialTheme.colorScheme.onPrimary, // Text color on primary
                        style = MaterialTheme.typography.titleSmall // Use titleSmall style
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Language Tabs
            TabRow(
                selectedTabIndex = selectedLanguageTabIndex, // Use state from ViewModel
                containerColor = MaterialTheme.colorScheme.surface, // Background for tabs
                contentColor = MaterialTheme.colorScheme.primary // Color for selected tab indicator/text
            ) {
                viewModel.languageTabs.forEachIndexed { index, title -> // Use tabs from ViewModel
                    Tab(
                        selected = selectedLanguageTabIndex == index,
                        onClick = { viewModel.onLanguageTabSelect(index) }, // Update ViewModel state
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedLanguageTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        enabled = !isLoading // Disable tabs while loading
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Conditional Content Area
            when {
                isLoading && !showFetchedContent -> {
                    // Show loading indicator in the content area only if no previous content is displayed
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                showFetchedContent               -> {
                    // Display fetched content
                    fetchedResult?.let { result ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Display content based on selected tab
                            when (selectedLanguageTabIndex) {
                                0 -> EnglishContent(result) // EN tab
                                1 -> JapaneseContent(result) // JA tab
                                2 -> ChineseContent(result) // ZH tab
                            }
                        }
                    }
                }

                else                             -> {
                    // Placeholder for when no data is fetched yet (Initial State)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Take remaining space
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TabBar Page") // Text from initial image
                    }
                }
            }
        }
    }

    // Error Dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.dismissErrorDialog() }) {
                    Text("OK")
                }
            }
        )
    }
}

// --- Helper Composable Functions to display content for each language tab ---

@Composable
fun EnglishContent(result: WordFetchResultJson) {
    Column {
        // Pronunciation
        Text(
            text = result.enPronunciation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 0.dp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Label
        Text(
            text = "explanation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Text
        Text(
            text = result.enExplanation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sentences Label
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sentences List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.getEnSentencesList().forEach { sentence ->
                SentenceItem(text = sentence) {
                    // Handle sentence click if needed
                    logger.debug { "EN Sentence clicked: $sentence" } // Replaced println
                }
            }
        }
        // TODO: Add Related Words if needed
    }
}

@Composable
fun JapaneseContent(result: WordFetchResultJson) {
    Column {
        // Pronunciation
        Text(
            text = result.jaPronunciation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 0.dp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Label
        Text(
            text = "explanation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Text
        Text(
            text = result.jaExplanation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sentences Label
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sentences List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.getJaSentencesList().forEach { sentence ->
                SentenceItem(text = sentence) {
                    // Handle sentence click if needed
                    logger.debug { "JA Sentence clicked: $sentence" } // Replaced println
                }
            }
        }
        // TODO: Add Related Words if needed
    }
}

@Composable
fun ChineseContent(result: WordFetchResultJson) {
    Column {
        // Pronunciation
        Text(
            text = result.zhPronunciation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 0.dp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Label
        Text(
            text = "explanation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Text
        Text(
            text = result.zhExplanation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sentences Label
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sentences List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.getZhSentencesList().forEach { sentence ->
                SentenceItem(text = sentence) {
                    // Handle sentence click if needed
                    logger.debug { "ZH Sentence clicked: $sentence" } // Replaced println
                }
            }
        }
        // TODO: Add Related Words if needed
    }
}

// --- Previews ---

@Preview
@Composable
fun WordFetchPagePreview_Initial() {
    MaterialTheme {
        Surface {
            // Create a dummy ViewModel for preview
            val dummyApi = WordFetchApi(apiKey = "dummy_key") // Dummy key for preview
            val dummyViewModel = WordFetchViewModel(api = dummyApi)
            // Set initial state explicitly for preview if needed, though default is initial
            dummyViewModel.resetPage()

            WordFetchPage(
                viewModel = dummyViewModel,
                onBack = {}
            )
        }
    }
}

@Preview
@Composable
fun WordFetchPagePreview_Loading() {
    MaterialTheme {
        Surface {
            // Create a dummy ViewModel for preview
            val dummyApi = WordFetchApi(apiKey = "dummy_key") // Dummy key for preview
            val dummyViewModel = WordFetchViewModel(api = dummyApi)
            // Simulate loading state
            dummyViewModel.isLoading = true
            dummyViewModel.onSearchQueryChange("test")

            WordFetchPage(
                viewModel = dummyViewModel,
                onBack = {}
            )
        }
    }
}


@Preview
@Composable
fun WordFetchPagePreview_Success() {
    MaterialTheme {
        Surface {
            // Create a dummy ViewModel for preview
            val dummyApi = WordFetchApi(apiKey = "dummy_key") // Dummy key for preview
            val dummyViewModel = WordFetchViewModel(api = dummyApi)
            // Simulate success state with dummy data
            dummyViewModel.fetchedResult = WordFetchResultJson(
                text = "热情",
                enExplanation = "Enthusiasm; passion; warmth; fervent; cordial",
                enSentences = "They welcomed us with great warmth and hospitaly.;She is full of enthusiasm for her work.",
                enRelatedWords = "Passion;zeal;ardor;excitement;fervor",
                enPronunciation = "rèqíng",
                jaExplanation = "熱意、情熱、心の温かさ。。",
                jaSentences = "彼らは私たちを熱意をもって歓迎した。;彼女は仕事に対して情熱的だ。",
                jaRelatedWords = "熱意(ねつい);情熱(じょうねつ);意欲(いよく);積極 的(せっきょくてき);活気(か",
                jaPronunciation = "",
                zhExplanation = "指积极、热烈的感情或态度。",
                zhSentences = "他们用极大的热情欢 迎了我们。;她对工作充满热情。",
                zhRelatedWords = "热心(rèxīn);激情(jīqíng);积极(jījí);热烈g",
                zhPronunciation = "rèqíng",
            )
            dummyViewModel.onSearchQueryChange("热情")
            dummyViewModel.selectedLanguageTabIndex = 0 // Start with EN tab

            WordFetchPage(
                viewModel = dummyViewModel,
                onBack = {}
            )
        }
    }
}

@Preview
@Composable
fun WordFetchPagePreview_Error() {
    MaterialTheme {
        Surface {
            // Create a dummy ViewModel for preview
            val dummyApi = WordFetchApi(apiKey = "dummy_key") // Dummy key for preview
            val dummyViewModel = WordFetchViewModel(api = dummyApi)
            // Simulate error state
            dummyViewModel.errorMessage = "Failed to connect to the API."
            dummyViewModel.onSearchQueryChange("test")

            WordFetchPage(
                viewModel = dummyViewModel,
                onBack = {}
            )
        }
    }
}
