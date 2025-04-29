package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.ApiKeyConfigRepository
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.data.api.WordFetchResultJson
import com.crossevol.wordbook.ui.components.RelatedWordItem
import com.crossevol.wordbook.data.WordRepository // Import for Mock
import com.crossevol.wordbook.ui.components.SentenceItem
import com.crossevol.wordbook.ui.svgicons.MyIconPack
import com.crossevol.wordbook.ui.svgicons.myiconpack.Save
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview


private val logger = KotlinLogging.logger {} // Add logger instance

@OptIn(
    ExperimentalMaterial3Api::class, // Keep for Scaffold etc.
    ExperimentalComposeUiApi::class, // Might be needed for focus or other interactions later
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
    val isResultSaved = viewModel.isResultSaved
    val isSaving = viewModel.isSaving

    // State for dropdown menu
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // State for the confirmation dialog when navigating back
    var showConfirmBackDialog by remember { mutableStateOf(false) }

    // Determine if fetched content should be shown (based on whether result is available)
    val showFetchedContent = fetchedResult != null

    // Determine if there are unsaved changes
    val hasUnsavedChanges = showFetchedContent && !isResultSaved

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicit") }, // Title from image
                navigationIcon = {
                    IconButton(onClick = {
                        // Check for unsaved changes before navigating back
                        if (hasUnsavedChanges) {
                            showConfirmBackDialog = true // Show confirmation dialog
                        } else {
                            onBack() // Navigate back directly
                        }
                    }) {
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
        },
        floatingActionButton = {
            // Show FAB only when content is fetched AND not yet saved AND not currently saving
            if (showFetchedContent && !isResultSaved) {
                FloatingActionButton(
                    onClick = {
                        if (!isSaving) { // Prevent multiple clicks while saving
                            viewModel.saveFetchedWord()
                        }
                    },
                    // Optionally change appearance when saving, but disabling is clearer
                ) {
                    Icon(MyIconPack.Save, contentDescription = "Save Word")
                }
            }
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
 
                    // Confirmation Dialog for Back Navigation
                    if (showConfirmBackDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmBackDialog = false }, // Dismiss on outside click or back press
                            title = { Text("Unsaved Changes") },
                            text = { Text("Do you want to save the fetched word before leaving?") },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.saveFetchedWord() // Attempt to save
                                    // Ideally, wait for save to finish, but for simplicity now:
                                    showConfirmBackDialog = false
                                    onBack() // Navigate back after initiating save
                                }) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                Button(onClick = {
                                    showConfirmBackDialog = false
                                    onBack() // Discard changes and navigate back
                                }) {
                                    Text("Discard")
                                }
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
            text = { Text(errorMessage) },
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
        // Pronunciation Label
        Text(
            text = "Pronunciation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

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
            text = "Explanation",
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
        Spacer(modifier = Modifier.height(24.dp)) // Add space before Related Words

        // Related Words Label
        Text(
            text = "Related Words",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Related Words List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // Slightly less space than sentences
            result.getEnRelatedWordsList().forEach { relatedWord ->
                RelatedWordItem(text = relatedWord) {
                    // Handle related word click if needed
                    logger.debug { "EN Related Word clicked: $relatedWord" }
                }
            }
        }
    }
}

@Composable
fun JapaneseContent(result: WordFetchResultJson) {
    Column {
        // Pronunciation Label
        Text(
            text = "Pronunciation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

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
            text = "Explanation",
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
        Spacer(modifier = Modifier.height(24.dp)) // Add space before Related Words

        // Related Words Label
        Text(
            text = "Related Words",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Related Words List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // Slightly less space than sentences
            result.getJaRelatedWordsList().forEach { relatedWord ->
                RelatedWordItem(text = relatedWord) {
                    // Handle related word click if needed
                    logger.debug { "JA Related Word clicked: $relatedWord" }
                }
            }
        }
    }
}

@Composable
fun ChineseContent(result: WordFetchResultJson) {
    Column {
        // Pronunciation Label
        Text(
            text = "Pronunciation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

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
            text = "Explanation",
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
        Spacer(modifier = Modifier.height(24.dp)) // Add space before Related Words

        // Related Words Label
        Text(
            text = "Related Words",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Related Words List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // Slightly less space than sentences
            result.getZhRelatedWordsList().forEach { relatedWord ->
                RelatedWordItem(text = relatedWord) {
                    // Handle related word click if needed
                    logger.debug { "ZH Related Word clicked: $relatedWord" }
                }
            }
        }
    }
}

// --- Mock ApiKeyConfigRepository for Previews ---
// This provides dummy data so the ViewModel can initialize modelOptions
private class MockApiKeyConfigRepository : ApiKeyConfigRepository(
    // Pass null or a mock database instance, as the repository methods
    // used by the ViewModel init block (getAllApiKeyConfigs) are mocked below.
    // In a real scenario, you might need a more sophisticated mock database.
    database = null!! // Use null!! to satisfy non-nullable parameter for mocking purposes
) {
     override fun getAllApiKeyConfigs(): List<ApiKeyConfig> {
        // Return a predefined list of dummy configs for preview
        return listOf(
            ApiKeyConfig(id = 1, alias = "Gemini Key 1", apiKey = "dummy_key_1", provider = "Google", model = "gemini-1.5-flash"),
            ApiKeyConfig(id = 2, alias = "Gemini Key 2", apiKey = "dummy_key_2", provider = "Google", model = "gemini-1.5-pro"),
            ApiKeyConfig(id = 3, alias = "OpenAI Key", apiKey = "dummy_key_3", provider = "OpenAI", model = "gpt-4o")
        )
    }

    // Implement other methods with dummy logic or throw exceptions if they are not expected to be called in previews
    override fun getApiKeyConfigById(id: Long): ApiKeyConfig? = null
    override fun insertApiKeyConfig(config: ApiKeyConfig) {}
    override fun updateApiKeyConfig(config: ApiKeyConfig) {}
    override fun deleteApiKeyConfigById(id: Long) {}
    override fun countConfigs(): Long = getAllApiKeyConfigs().size.toLong()
}
 
// --- Mock WordRepository for Previews ---
// Needed because WordFetchViewModel now depends on it.
private class MockWordRepository : WordRepository(database = null!!) { // Use null!! for mocking
    override fun saveWordDetails(
        title: String,
        languageCode: String,
        explanation: String?,
        sentences: List<String>,
        pronunciation: String?,
        relatedWords: List<String>,
        rating: Long
    ) {
        logger.debug { "[Preview Mock] Saving word: $title ($languageCode)" }
        // No actual DB interaction in mock
    }
    // Implement other methods if needed by previews, otherwise leave empty or throw
}
 
// --- Previews ---

@Preview
@Composable
fun WordFetchPagePreview_Initial() {
    MaterialTheme {
        Surface {
            // Create a dummy ViewModel for preview
            val dummyApi = WordFetchApi() // API client no longer needs key in constructor
            val dummyRepo = MockApiKeyConfigRepository() // Create mock repository
            val dummyWordRepo = MockWordRepository() // Create mock word repository
            val dummyViewModel = WordFetchViewModel(
                api = dummyApi,
                apiKeyConfigRepository = dummyRepo,
                wordRepository = dummyWordRepo // Pass mock word repo
            )
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
            val dummyApi = WordFetchApi() // API client no longer needs key in constructor
            val dummyRepo = MockApiKeyConfigRepository() // Create mock repository
            val dummyWordRepo = MockWordRepository() // Create mock word repository
            val dummyViewModel = WordFetchViewModel(
                api = dummyApi,
                apiKeyConfigRepository = dummyRepo,
                wordRepository = dummyWordRepo // Pass mock word repo
            )
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
            val dummyApi = WordFetchApi() // API client no longer needs key in constructor
            val dummyRepo = MockApiKeyConfigRepository() // Create mock repository
            val dummyWordRepo = MockWordRepository() // Create mock word repository
            val dummyViewModel = WordFetchViewModel(
                api = dummyApi,
                apiKeyConfigRepository = dummyRepo,
                wordRepository = dummyWordRepo // Pass mock word repo
            )
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
            val dummyApi = WordFetchApi() // API client no longer needs key in constructor
            val dummyRepo = MockApiKeyConfigRepository() // Create mock repository
            val dummyWordRepo = MockWordRepository() // Create mock word repository
            val dummyViewModel = WordFetchViewModel(
                api = dummyApi,
                apiKeyConfigRepository = dummyRepo,
                wordRepository = dummyWordRepo // Pass mock word repo
            )
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
