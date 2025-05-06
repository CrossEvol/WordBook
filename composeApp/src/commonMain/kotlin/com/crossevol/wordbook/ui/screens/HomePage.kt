package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.* // Added this import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import com.crossevol.wordbook.data.SettingsRepository
import com.crossevol.wordbook.data.WordRepository
import com.crossevol.wordbook.data.mock.sampleWordListEN
import com.crossevol.wordbook.data.mock.sampleWordListJA
import com.crossevol.wordbook.data.mock.sampleWordListZH
import com.crossevol.wordbook.data.model.FilterOption
import com.crossevol.wordbook.data.model.WordItemDB
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.FilterDropdownMenu
import com.crossevol.wordbook.ui.components.WordListItem
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {} // Add logger instance


// Data class to represent bottom navigation items
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// Define bottom navigation items
val bottomNavItems = listOf(
    BottomNavItem(
        "Browse",
        Icons.Filled.Home,
        "home"
    ),
    BottomNavItem(
        "Review",
        Icons.Filled.Notifications,
        "review"
    ), // Placeholder icon
    BottomNavItem(
        "Settings",
        Icons.Filled.Settings,
        "settings"
    )
)


/**
 * The main Home Page screen composable using Material 3.
 * Includes TopAppBar, Bottom Navigation Bar, and a list of WordListItems.
 */
@Composable
fun HomePage(
    settingsRepository: SettingsRepository?, // Accept SettingsRepository
    wordRepository: WordRepository?, // Inject WordRepository (make nullable for previews)
    snackbarHostState: SnackbarHostState, // Accept SnackbarHostState
    // words: List<WordItemUI> = sampleWordList, // Removed hardcoded sample list
    // onFilterClick: () -> Unit = {}, // Filter click is handled internally now
    onWordItemClick: (WordItemUI) -> Unit, // Changed: Make this non-optional for navigation
    onNavigate: (String) -> Unit = {} // Callback for bottom navigation
) {
    // State for tracking the selected bottom navigation item index
    var selectedItemIndex by remember { mutableStateOf(0) }
    // State for filter dropdown visibility
    var showFilterMenu by remember { mutableStateOf(false) }
    // State for selected filter options - Default to ALL
    var selectedFilters by remember { mutableStateOf(setOf(FilterOption.ALL)) }

    // Read LocalInspectionMode outside the remember block
    val isPreview = LocalInspectionMode.current

    // State for locale selection (New)
    // Initialize locale from settings or use default if repository is null (for preview)
    var currentLocale by remember {
        // Use the value read outside
        mutableStateOf(
            if (isPreview) "EN" else settingsRepository?.getLocale() ?: "EN"
        )
    }
    var showLocaleMenu by remember { mutableStateOf(false) } // Locale dropdown visibility

    // State to hold the list of words fetched from the repository
    var wordsToDisplay by remember { mutableStateOf<List<WordItemUI>>(emptyList()) }

    // Fetch words when locale or repository changes
    LaunchedEffect(
        currentLocale,
        wordRepository
    ) {
        if (wordRepository != null) {
            logger.debug { "HomePage: Fetching words for locale: $currentLocale" }
            wordsToDisplay = wordRepository.getWordItemsForLanguage(currentLocale)
            logger.debug { "HomePage: Fetched ${wordsToDisplay.size} words." }
        } else if (!isPreview) { // Only log warning if not in preview mode
            // Handle the case where the repository is not yet available (log or show message)
            logger.warn { "HomePage: WordRepository is null. Cannot fetch words." }
            wordsToDisplay = emptyList() // Ensure list is empty if repo is null
        }
        // In preview mode, wordsToDisplay will remain empty unless set otherwise for preview
    }


    // Logic to handle filter option toggles
    val onFilterOptionToggle = { option: FilterOption, isSelected: Boolean ->
        selectedFilters = when (option) {
            FilterOption.ALL -> setOf(FilterOption.ALL) // Selecting ALL overrides others
            FilterOption.NONE -> setOf(FilterOption.NONE) // Selecting NONE overrides others
            else -> {
                // If toggling an individual option, remove ALL/NONE and update the set
                val currentFilters = selectedFilters.toMutableSet()
                currentFilters.remove(FilterOption.ALL)
                currentFilters.remove(FilterOption.NONE)
                if (isSelected) {
                    currentFilters.add(option)
                } else {
                    currentFilters.remove(option)
                }
                // If all individuals are selected, maybe switch to ALL? (Optional enhancement)
                // If no individuals are selected, maybe switch to NONE? (Optional enhancement)
                if (currentFilters.isEmpty()) setOf(FilterOption.NONE) else currentFilters // Default to NONE if empty
            }
        }
        // Close the menu after selection? Maybe not, allow multiple toggles.
        // showFilterMenu = false
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse") },
                actions = {

                    // Locale Selector (New)
                    Box { // Box to anchor the dropdown
                        IconButton(onClick = { showLocaleMenu = true }) {
                            Text(
                                currentLocale, // Display current locale state
                                style = MaterialTheme.typography.h2, // Or another suitable style
                                color = MaterialTheme.colors.onPrimary // Match TopAppBar text color (assuming primary is dark enough)
                            )
                        }
                        DropdownMenu(
                            expanded = showLocaleMenu,
                            onDismissRequest = { showLocaleMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    settingsRepository?.setLocale("EN") // Save to settings
                                    currentLocale = "EN" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            ) {
                                Text("English (EN)")
                            }
                            DropdownMenuItem(
                                onClick = {
                                    settingsRepository?.setLocale("JA") // Save to settings
                                    currentLocale = "JA" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            ) {
                                Text("日本語 (JA)")
                            }
                            DropdownMenuItem(
                                onClick = {
                                    settingsRepository?.setLocale("ZH") // Save to settings
                                    currentLocale = "ZH" // Update local state
                                    showLocaleMenu = false
                                    // TODO: Implement actual locale change logic if needed elsewhere
                                }
                            ) {
                                Text("中文 (ZH)")
                            }
                        }
                    }

                    // Existing Filter Icon and Dropdown Menu
                    Box { // Box to anchor the DropdownMenu
                        IconButton(onClick = { showFilterMenu = true }) { // Open the menu
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Filter words"
                            )
                        }
                        // The Filter Dropdown Menu itself
                        FilterDropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            selectedOptions = selectedFilters,
                            onOptionToggle = onFilterOptionToggle
                        )
                    }
                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colors.primary, // Use primary color from theme
//                    titleContentColor = MaterialTheme.colors.onPrimary,
//                    actionIconContentColor = MaterialTheme.colors.onPrimary
//                )
            )
        },
        bottomBar = {
            // Use NavigationBar for Material 3 bottom navigation
            BottomNavigation( // Changed from NavigationBar to BottomNavigation for Material 2
                backgroundColor = MaterialTheme.colors.primaryVariant, // Example color
                contentColor = MaterialTheme.colors.onPrimary // Example color
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    BottomNavigationItem( // Changed from NavigationBarItem to BottomNavigationItem
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            onNavigate(item.route) // Trigger navigation callback
                        },
                        // Customize colors using NavigationBarItemDefaults if needed
                        // Material 2 BottomNavigationItem doesn't have a direct 'colors' parameter like M3
                        // You would typically control colors via the parent BottomNavigation or theme
                        selectedContentColor = MaterialTheme.colors.primary, // Example
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium) // Example
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("fetch") }, // Navigate to fetch screen
                backgroundColor = MaterialTheme.colors.secondary, // Example color
                contentColor = MaterialTheme.colors.onSecondary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add new word"
                )
            }
        }
    ) { innerPadding ->
        // Call the extracted WordList composable
        WordList(
            // Pass the selected list based on locale
            // Use the fetched words state. If in preview and repo is null, use sample EN data.
            words = if (isPreview && wordRepository == null) sampleWordListEN else wordsToDisplay,
            // Alternative for preview: words = if (isPreview) sampleWordListEN else wordsToDisplay,
            // This depends if you want HomePagePreview to show data even if repo is null
            // words = wordsToDisplay, // Pass the fetched words state
            visibleFilters = selectedFilters,
            onWordItemClick = onWordItemClick,
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        )
    }
}

/**
 * Displays the list of word items using a LazyColumn.
 *
 * @param words The list of [WordItemDB] to display.
 * @param visibleFilters The set of [FilterOption] to apply to each item.
 * @param onWordItemClick Callback invoked when a word item is clicked.
 * @param modifier Modifier for the LazyColumn.
 */
@Composable
fun WordList(
    words: List<WordItemUI>,
    visibleFilters: Set<FilterOption>,
    onWordItemClick: (WordItemUI) -> Unit, // Pass the click handler down
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            words,
            key = { it.id }) { word -> // Use word id as key for better performance
            WordListItem(
                item = word,
                visibleFilters = visibleFilters, // Pass the selected filters
                // Apply the clickable modifier here, calling the passed lambda
                modifier = Modifier.clickable { onWordItemClick(word) }
            )
        }
    }
}


@Preview
@Composable
fun HomePagePreview() {
    MaterialTheme { // Ensure MaterialTheme is applied for preview
        val dummySnackbarHostState = SnackbarHostState() // Create mock SnackbarHostState

        // Provide dummy lambda, null settings repo, and null word repo for preview.
        // The LaunchedEffect won't run with a null repo in preview.
        // The WordList call inside HomePage is modified to show sample EN data in this case.
        HomePage(
            settingsRepository = null,
            wordRepository = null, // Pass null for preview
            snackbarHostState = dummySnackbarHostState,
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewEN() {
    MaterialTheme {
        WordList(
            words = sampleWordListEN,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewJA() {
    MaterialTheme {
        WordList(
            words = sampleWordListJA,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}

@Preview
@Composable
fun WordListPreviewZH() {
    MaterialTheme {
        WordList(
            words = sampleWordListZH,
            visibleFilters = setOf(FilterOption.ALL),
            onWordItemClick = {}
        )
    }
}
